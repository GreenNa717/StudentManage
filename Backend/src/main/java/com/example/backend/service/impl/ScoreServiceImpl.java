package com.example.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.common.constants.RoleConstants;
import com.example.backend.common.exception.BusinessException;
import com.example.backend.common.result.ErrorCode;
import com.example.backend.common.result.PageResult;
import com.example.backend.common.util.SecurityUtils;
import com.example.backend.dto.request.ScoreRequest;
import com.example.backend.dto.response.ScoreImportResultVO;
import com.example.backend.dto.response.ScoreStatisticsVO;
import com.example.backend.dto.response.ScoreVO;
import com.example.backend.entity.Course;
import com.example.backend.entity.Score;
import com.example.backend.entity.Student;
import com.example.backend.mapper.CourseMapper;
import com.example.backend.mapper.ScoreMapper;
import com.example.backend.mapper.StudentMapper;
import com.example.backend.service.ScoreService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ScoreServiceImpl extends ServiceImpl<ScoreMapper, Score> implements ScoreService {

    private final StudentMapper studentMapper;
    private final CourseMapper courseMapper;

    public ScoreServiceImpl(StudentMapper studentMapper, CourseMapper courseMapper) {
        this.studentMapper = studentMapper;
        this.courseMapper = courseMapper;
    }

    @Override
    public PageResult<ScoreVO> page(Integer page, Integer size, Long studentId, Long courseId, String semester) {
        LambdaQueryWrapper<Score> wrapper = buildScopedWrapper(studentId, courseId, semester);
        wrapper.orderByDesc(Score::getCreateTime);

        Page<Score> pageResult = baseMapper.selectPage(new Page<>(page, size), wrapper);

        List<ScoreVO> records = pageResult.getRecords().stream().map(this::toVO).toList();
        return new PageResult<>(pageResult.getCurrent(), pageResult.getSize(), pageResult.getTotal(), records);
    }

    @Override
    public ScoreVO getDetail(Long id) {
        Score score = baseMapper.selectById(id);
        if (score == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "成绩记录不存在");
        }
        ensureReadable(score);
        return toVO(score);
    }

    @Override
    public void create(ScoreRequest request) {
        checkStudent(request.getStudentId());
        Course course = checkCourse(request.getCourseId());
        ensureWritable(course);
        checkUnique(request.getStudentId(), request.getCourseId(), request.getSemester(), null);
        Score score = new Score();
        BeanUtils.copyProperties(request, score);
        baseMapper.insert(score);
    }

    @Override
    public void update(Long id, ScoreRequest request) {
        Score score = baseMapper.selectById(id);
        if (score == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "成绩记录不存在");
        }
        checkStudent(request.getStudentId());
        Course course = checkCourse(request.getCourseId());
        ensureWritable(course);
        checkUnique(request.getStudentId(), request.getCourseId(), request.getSemester(), id);
        BeanUtils.copyProperties(request, score);
        score.setId(id);
        baseMapper.updateById(score);
    }

    @Override
    public void delete(Long id) {
        Score score = baseMapper.selectById(id);
        if (score == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "成绩记录不存在");
        }
        baseMapper.deleteById(id);
    }

    @Override
    public List<ScoreStatisticsVO> getStatistics(Long courseId, String semester) {
        LambdaQueryWrapper<Score> wrapper = buildScopedWrapper(null, courseId, semester);
        List<Score> scores = baseMapper.selectList(wrapper);

        return scores.stream()
                .collect(java.util.stream.Collectors.groupingBy(Score::getCourseId))
                .entrySet().stream()
                .map(entry -> {
                    List<Score> courseScores = entry.getValue();
                    Course course = courseMapper.selectById(entry.getKey());

                    ScoreStatisticsVO vo = new ScoreStatisticsVO();
                    vo.setCourseId(entry.getKey());
                    vo.setCourseName(course != null ? course.getName() : "未知");
                    vo.setTotalCount((long) courseScores.size());

                    BigDecimal avg = courseScores.stream()
                            .map(Score::getScore)
                            .reduce(BigDecimal.ZERO, BigDecimal::add)
                            .divide(BigDecimal.valueOf(courseScores.size()), 2, RoundingMode.HALF_UP);
                    vo.setAvgScore(avg);

                    vo.setMaxScore(courseScores.stream().map(Score::getScore).max(BigDecimal::compareTo).orElse(BigDecimal.ZERO));
                    vo.setMinScore(courseScores.stream().map(Score::getScore).min(BigDecimal::compareTo).orElse(BigDecimal.ZERO));

                    long passCount = courseScores.stream().filter(s -> s.getScore().compareTo(BigDecimal.valueOf(60)) >= 0).count();
                    vo.setPassRate(BigDecimal.valueOf(passCount)
                            .divide(BigDecimal.valueOf(courseScores.size()), 4, RoundingMode.HALF_UP)
                            .multiply(BigDecimal.valueOf(100))
                            .setScale(2, RoundingMode.HALF_UP));

                    return vo;
                })
                .toList();
    }

    @Override
    public ScoreImportResultVO importScores(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "导入文件不能为空");
        }

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
            ScoreImportResultVO result = new ScoreImportResultVO();
            String headerLine = reader.readLine();
            if (headerLine == null) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "导入文件内容为空");
            }

            List<String> header = parseCsvLine(stripBom(headerLine));
            validateHeader(header);

            String line;
            int rowIndex = 1;
            while ((line = reader.readLine()) != null) {
                rowIndex++;
                if (line.isBlank()) {
                    continue;
                }
                result.setTotalRows(result.getTotalRows() + 1);
                try {
                    int imported = importRow(line);
                    if (imported == 1) {
                        result.setCreatedCount(result.getCreatedCount() + 1);
                    } else if (imported == 2) {
                        result.setUpdatedCount(result.getUpdatedCount() + 1);
                    }
                } catch (BusinessException ex) {
                    result.setSkippedCount(result.getSkippedCount() + 1);
                    result.getErrorMessages().add("第" + rowIndex + "行: " + ex.getMessage());
                } catch (Exception ex) {
                    result.setSkippedCount(result.getSkippedCount() + 1);
                    result.getErrorMessages().add("第" + rowIndex + "行: 数据格式错误");
                }
            }
            return result;
        } catch (IOException ex) {
            throw new BusinessException(ErrorCode.SERVER_ERROR, "读取导入文件失败");
        }
    }

    @Override
    public ByteArrayInputStream exportScores(Long studentId, Long courseId, String semester) {
        LambdaQueryWrapper<Score> wrapper = buildScopedWrapper(studentId, courseId, semester);
        wrapper.orderByDesc(Score::getCreateTime);
        List<Score> scores = baseMapper.selectList(wrapper);

        StringBuilder builder = new StringBuilder();
        builder.append('\uFEFF');
        builder.append("studentNo,studentName,courseNo,courseName,score,semester\n");
        for (Score score : scores) {
            Student student = studentMapper.selectById(score.getStudentId());
            Course course = courseMapper.selectById(score.getCourseId());
            builder.append(csv(student == null ? "" : student.getStudentNo())).append(',')
                    .append(csv(student == null ? "" : student.getName())).append(',')
                    .append(csv(course == null ? "" : course.getCourseNo())).append(',')
                    .append(csv(course == null ? "" : course.getName())).append(',')
                    .append(score.getScore()).append(',')
                    .append(csv(score.getSemester()))
                    .append('\n');
        }
        return new ByteArrayInputStream(builder.toString().getBytes(StandardCharsets.UTF_8));
    }

    @Override
    public ByteArrayInputStream exportTemplate() {
        StringBuilder builder = new StringBuilder();
        builder.append('\uFEFF');
        builder.append("studentNo,courseNo,score,semester\n");
        builder.append("2023001001,CS101,95,2024-2025-1\n");
        return new ByteArrayInputStream(builder.toString().getBytes(StandardCharsets.UTF_8));
    }

    private void checkStudent(Long studentId) {
        Student student = studentMapper.selectById(studentId);
        if (student == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "学生不存在");
        }
    }

    private Course checkCourse(Long courseId) {
        Course course = courseMapper.selectById(courseId);
        if (course == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "课程不存在");
        }
        return course;
    }

    private void checkUnique(Long studentId, Long courseId, String semester, Long excludeId) {
        LambdaQueryWrapper<Score> wrapper = new LambdaQueryWrapper<Score>()
                .eq(Score::getStudentId, studentId)
                .eq(Score::getCourseId, courseId)
                .eq(Score::getSemester, semester);
        if (excludeId != null) {
            wrapper.ne(Score::getId, excludeId);
        }
        Long count = baseMapper.selectCount(wrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "该学生此课程本学期成绩已存在");
        }
    }

    private LambdaQueryWrapper<Score> buildScopedWrapper(Long studentId, Long courseId, String semester) {
        LambdaQueryWrapper<Score> wrapper = new LambdaQueryWrapper<>();
        applyDataScope(wrapper);
        if (studentId != null) {
            wrapper.eq(Score::getStudentId, studentId);
        }
        if (courseId != null) {
            wrapper.eq(Score::getCourseId, courseId);
        }
        if (semester != null && !semester.isBlank()) {
            wrapper.eq(Score::getSemester, semester);
        }
        return wrapper;
    }

    private void applyDataScope(LambdaQueryWrapper<Score> wrapper) {
        var loginUser = SecurityUtils.getLoginUser();
        if (RoleConstants.STUDENT.equals(loginUser.getRole())) {
            if (loginUser.getRefId() == null) {
                wrapper.eq(Score::getId, -1L);
                return;
            }
            wrapper.eq(Score::getStudentId, loginUser.getRefId());
            return;
        }

        if (RoleConstants.TEACHER.equals(loginUser.getRole())) {
            if (loginUser.getRefId() == null) {
                wrapper.eq(Score::getId, -1L);
                return;
            }
            List<Long> courseIds = courseMapper.selectList(new LambdaQueryWrapper<Course>()
                            .eq(Course::getTeacherId, loginUser.getRefId()))
                    .stream()
                    .map(Course::getId)
                    .collect(Collectors.toList());
            if (courseIds.isEmpty()) {
                wrapper.eq(Score::getId, -1L);
                return;
            }
            wrapper.in(Score::getCourseId, courseIds);
        }
    }

    private void ensureReadable(Score score) {
        var loginUser = SecurityUtils.getLoginUser();
        if (RoleConstants.ADMIN.equals(loginUser.getRole())) {
            return;
        }
        if (RoleConstants.STUDENT.equals(loginUser.getRole())) {
            if (loginUser.getRefId() == null || !loginUser.getRefId().equals(score.getStudentId())) {
                throw new BusinessException(ErrorCode.FORBIDDEN, "只能查看本人的成绩信息");
            }
            return;
        }
        if (RoleConstants.TEACHER.equals(loginUser.getRole())) {
            Course course = courseMapper.selectById(score.getCourseId());
            if (course == null || loginUser.getRefId() == null || !loginUser.getRefId().equals(course.getTeacherId())) {
                throw new BusinessException(ErrorCode.FORBIDDEN, "只能查看本人授课课程的成绩");
            }
        }
    }

    private void ensureWritable(Course course) {
        var loginUser = SecurityUtils.getLoginUser();
        if (RoleConstants.TEACHER.equals(loginUser.getRole())
                && (loginUser.getRefId() == null || !loginUser.getRefId().equals(course.getTeacherId()))) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只能维护本人授课课程的成绩");
        }
    }

    private int importRow(String line) {
        List<String> columns = parseCsvLine(line);
        if (columns.size() < 4) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "导入列不足，需包含 studentNo,courseNo,score,semester");
        }

        String studentNo = columns.get(0).trim();
        String courseNo = columns.get(1).trim();
        String scoreValue = columns.get(2).trim();
        String semester = columns.get(3).trim();

        if (studentNo.isBlank() || courseNo.isBlank() || scoreValue.isBlank() || semester.isBlank()) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "学号、课程号、成绩、学期均不能为空");
        }

        Student student = studentMapper.selectOne(new LambdaQueryWrapper<Student>().eq(Student::getStudentNo, studentNo));
        if (student == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "学生学号不存在: " + studentNo);
        }
        Course course = courseMapper.selectOne(new LambdaQueryWrapper<Course>().eq(Course::getCourseNo, courseNo));
        if (course == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "课程号不存在: " + courseNo);
        }
        ensureWritable(course);

        BigDecimal numericScore;
        try {
            numericScore = new BigDecimal(scoreValue);
        } catch (NumberFormatException ex) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "成绩不是合法数字");
        }
        if (numericScore.compareTo(BigDecimal.ZERO) < 0 || numericScore.compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "成绩必须在0-100之间");
        }

        Score existing = baseMapper.selectOne(new LambdaQueryWrapper<Score>()
                .eq(Score::getStudentId, student.getId())
                .eq(Score::getCourseId, course.getId())
                .eq(Score::getSemester, semester));

        if (existing != null) {
            existing.setScore(numericScore);
            baseMapper.updateById(existing);
            return 2;
        }

        Score score = new Score();
        score.setStudentId(student.getId());
        score.setCourseId(course.getId());
        score.setScore(numericScore);
        score.setSemester(semester);
        baseMapper.insert(score);
        return 1;
    }

    private void validateHeader(List<String> header) {
        List<String> normalized = header.stream().map(item -> item.trim().toLowerCase()).toList();
        List<String> expected = List.of("studentno", "courseno", "score", "semester");
        if (normalized.size() < expected.size() || !normalized.subList(0, expected.size()).equals(expected)) {
            throw new BusinessException(ErrorCode.PARAM_ERROR, "导入模板表头必须为 studentNo,courseNo,score,semester");
        }
    }

    private List<String> parseCsvLine(String line) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char ch = line.charAt(i);
            if (ch == '"') {
                if (inQuotes && i + 1 < line.length() && line.charAt(i + 1) == '"') {
                    current.append('"');
                    i++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (ch == ',' && !inQuotes) {
                result.add(current.toString());
                current.setLength(0);
            } else {
                current.append(ch);
            }
        }
        result.add(current.toString());
        return result;
    }

    private String csv(String value) {
        String safe = value == null ? "" : value;
        if (safe.contains(",") || safe.contains("\"") || safe.contains("\n")) {
            return "\"" + safe.replace("\"", "\"\"") + "\"";
        }
        return safe;
    }

    private String stripBom(String value) {
        if (value != null && !value.isEmpty() && value.charAt(0) == '\uFEFF') {
            return value.substring(1);
        }
        return value;
    }

    private ScoreVO toVO(Score score) {
        ScoreVO vo = new ScoreVO();
        BeanUtils.copyProperties(score, vo);
        Student student = studentMapper.selectById(score.getStudentId());
        if (student != null) {
            vo.setStudentName(student.getName());
            vo.setStudentNo(student.getStudentNo());
        }
        Course course = courseMapper.selectById(score.getCourseId());
        if (course != null) {
            vo.setCourseName(course.getName());
        }
        return vo;
    }
}

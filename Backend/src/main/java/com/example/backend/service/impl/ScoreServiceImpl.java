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

import java.math.BigDecimal;
import java.math.RoundingMode;
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
        LambdaQueryWrapper<Score> wrapper = new LambdaQueryWrapper<>();
        applyDataScope(wrapper);
        if (courseId != null) {
            wrapper.eq(Score::getCourseId, courseId);
        }
        if (semester != null && !semester.isBlank()) {
            wrapper.eq(Score::getSemester, semester);
        }

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

package com.example.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.common.constants.RoleConstants;
import com.example.backend.common.exception.BusinessException;
import com.example.backend.common.result.ErrorCode;
import com.example.backend.common.result.PageResult;
import com.example.backend.common.util.SecurityUtils;
import com.example.backend.dto.request.CourseRequest;
import com.example.backend.dto.response.CourseVO;
import com.example.backend.entity.Course;
import com.example.backend.entity.Score;
import com.example.backend.entity.Teacher;
import com.example.backend.mapper.CourseMapper;
import com.example.backend.mapper.ScoreMapper;
import com.example.backend.mapper.TeacherMapper;
import com.example.backend.service.CourseService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CourseServiceImpl extends ServiceImpl<CourseMapper, Course> implements CourseService {

    private final TeacherMapper teacherMapper;
    private final ScoreMapper scoreMapper;

    public CourseServiceImpl(TeacherMapper teacherMapper, ScoreMapper scoreMapper) {
        this.teacherMapper = teacherMapper;
        this.scoreMapper = scoreMapper;
    }

    @Override
    public PageResult<CourseVO> page(Integer page, Integer size, String keyword, Long teacherId) {
        LambdaQueryWrapper<Course> wrapper = new LambdaQueryWrapper<>();
        applyDataScope(wrapper);
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(Course::getName, keyword).or().like(Course::getCourseNo, keyword));
        }
        if (teacherId != null) {
            wrapper.eq(Course::getTeacherId, teacherId);
        }
        wrapper.orderByDesc(Course::getCreateTime);

        Page<Course> pageResult = baseMapper.selectPage(new Page<>(page, size), wrapper);

        List<CourseVO> records = pageResult.getRecords().stream().map(this::toVO).toList();
        return new PageResult<>(pageResult.getCurrent(), pageResult.getSize(), pageResult.getTotal(), records);
    }

    @Override
    public CourseVO getDetail(Long id) {
        Course course = baseMapper.selectById(id);
        if (course == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "课程不存在");
        }
        ensureReadable(course);
        return toVO(course);
    }

    @Override
    public void create(CourseRequest request) {
        checkTeacher(request.getTeacherId());
        Long count = baseMapper.selectCount(
                new LambdaQueryWrapper<Course>().eq(Course::getCourseNo, request.getCourseNo()));
        if (count > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "课程号已存在");
        }
        Course course = new Course();
        BeanUtils.copyProperties(request, course);
        baseMapper.insert(course);
    }

    @Override
    public void update(Long id, CourseRequest request) {
        Course course = baseMapper.selectById(id);
        if (course == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "课程不存在");
        }
        checkTeacher(request.getTeacherId());
        Long count = baseMapper.selectCount(
                new LambdaQueryWrapper<Course>().eq(Course::getCourseNo, request.getCourseNo()).ne(Course::getId, id));
        if (count > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "课程号已存在");
        }
        BeanUtils.copyProperties(request, course);
        course.setId(id);
        baseMapper.updateById(course);
    }

    @Override
    public void delete(Long id) {
        Course course = baseMapper.selectById(id);
        if (course == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "课程不存在");
        }
        Long scoreCount = scoreMapper.selectCount(
                new LambdaQueryWrapper<Score>().eq(Score::getCourseId, id));
        if (scoreCount > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "该课程下存在成绩记录，不可删除");
        }
        baseMapper.deleteById(id);
    }

    private void checkTeacher(Long teacherId) {
        Teacher teacher = teacherMapper.selectById(teacherId);
        if (teacher == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "授课教师不存在");
        }
    }

    private void applyDataScope(LambdaQueryWrapper<Course> wrapper) {
        var loginUser = SecurityUtils.getLoginUser();
        if (RoleConstants.TEACHER.equals(loginUser.getRole())) {
            if (loginUser.getRefId() == null) {
                wrapper.eq(Course::getId, -1L);
                return;
            }
            wrapper.eq(Course::getTeacherId, loginUser.getRefId());
            return;
        }

        if (RoleConstants.STUDENT.equals(loginUser.getRole())) {
            if (loginUser.getRefId() == null) {
                wrapper.eq(Course::getId, -1L);
                return;
            }
            List<Long> courseIds = scoreMapper.selectList(new LambdaQueryWrapper<Score>()
                            .eq(Score::getStudentId, loginUser.getRefId()))
                    .stream()
                    .map(Score::getCourseId)
                    .distinct()
                    .collect(Collectors.toList());
            if (courseIds.isEmpty()) {
                wrapper.eq(Course::getId, -1L);
                return;
            }
            wrapper.in(Course::getId, courseIds);
        }
    }

    private void ensureReadable(Course course) {
        var loginUser = SecurityUtils.getLoginUser();
        if (RoleConstants.ADMIN.equals(loginUser.getRole())) {
            return;
        }
        if (RoleConstants.TEACHER.equals(loginUser.getRole())) {
            if (loginUser.getRefId() == null || !loginUser.getRefId().equals(course.getTeacherId())) {
                throw new BusinessException(ErrorCode.FORBIDDEN, "只能查看本人授课的课程");
            }
            return;
        }
        if (RoleConstants.STUDENT.equals(loginUser.getRole())) {
            if (loginUser.getRefId() == null) {
                throw new BusinessException(ErrorCode.FORBIDDEN, "只能查看本人的课程信息");
            }
            Long count = scoreMapper.selectCount(new LambdaQueryWrapper<Score>()
                    .eq(Score::getStudentId, loginUser.getRefId())
                    .eq(Score::getCourseId, course.getId()));
            if (count == 0) {
                throw new BusinessException(ErrorCode.FORBIDDEN, "只能查看本人的课程信息");
            }
        }
    }

    private CourseVO toVO(Course course) {
        CourseVO vo = new CourseVO();
        BeanUtils.copyProperties(course, vo);
        Teacher teacher = teacherMapper.selectById(course.getTeacherId());
        if (teacher != null) {
            vo.setTeacherName(teacher.getName());
        }
        return vo;
    }
}

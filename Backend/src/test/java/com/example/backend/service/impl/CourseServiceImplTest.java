package com.example.backend.service.impl;

import com.example.backend.common.constants.RoleConstants;
import com.example.backend.common.exception.BusinessException;
import com.example.backend.common.result.ErrorCode;
import com.example.backend.entity.Course;
import com.example.backend.mapper.CourseMapper;
import com.example.backend.mapper.ScoreMapper;
import com.example.backend.mapper.TeacherMapper;
import com.example.backend.support.SecurityTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CourseServiceImplTest extends SecurityTestSupport {

    @Mock
    private TeacherMapper teacherMapper;

    @Mock
    private ScoreMapper scoreMapper;

    @Mock
    private CourseMapper courseMapper;

    private CourseServiceImpl courseService;

    @BeforeEach
    void setUp() {
        courseService = new CourseServiceImpl(teacherMapper, scoreMapper);
        ReflectionTestUtils.setField(courseService, "baseMapper", courseMapper);
    }

    @Test
    void teacherCannotViewOtherTeachersCourse() {
        authenticate(RoleConstants.TEACHER, 2L);
        Course course = new Course();
        course.setId(10L);
        course.setTeacherId(9L);
        when(courseMapper.selectById(10L)).thenReturn(course);

        BusinessException exception = assertThrows(BusinessException.class, () -> courseService.getDetail(10L));

        assertEquals(ErrorCode.FORBIDDEN, exception.getCode());
        assertEquals("只能查看本人授课的课程", exception.getMessage());
    }

    @Test
    void studentCannotViewUnrelatedCourse() {
        authenticate(RoleConstants.STUDENT, 3L);
        Course course = new Course();
        course.setId(10L);
        when(courseMapper.selectById(10L)).thenReturn(course);
        when(scoreMapper.selectCount(any())).thenReturn(0L);

        BusinessException exception = assertThrows(BusinessException.class, () -> courseService.getDetail(10L));

        assertEquals(ErrorCode.FORBIDDEN, exception.getCode());
        assertEquals("只能查看本人的课程信息", exception.getMessage());
    }
}

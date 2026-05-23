package com.example.backend.service.impl;

import com.example.backend.common.constants.RoleConstants;
import com.example.backend.common.exception.BusinessException;
import com.example.backend.common.result.ErrorCode;
import com.example.backend.dto.request.ScoreRequest;
import com.example.backend.entity.Course;
import com.example.backend.entity.Score;
import com.example.backend.entity.Student;
import com.example.backend.mapper.CourseMapper;
import com.example.backend.mapper.ScoreMapper;
import com.example.backend.mapper.StudentMapper;
import com.example.backend.support.SecurityTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ScoreServiceImplTest extends SecurityTestSupport {

    @Mock
    private StudentMapper studentMapper;

    @Mock
    private CourseMapper courseMapper;

    @Mock
    private ScoreMapper scoreMapper;

    private ScoreServiceImpl scoreService;

    @BeforeEach
    void setUp() {
        scoreService = new ScoreServiceImpl(studentMapper, courseMapper);
        ReflectionTestUtils.setField(scoreService, "baseMapper", scoreMapper);
    }

    @Test
    void teacherCannotCreateScoreForAnotherTeachersCourse() {
        authenticate(RoleConstants.TEACHER, 2L);
        ScoreRequest request = new ScoreRequest();
        request.setStudentId(1L);
        request.setCourseId(5L);
        request.setScore(BigDecimal.valueOf(96));
        request.setSemester("2024-2025-1");

        Student student = new Student();
        student.setId(1L);
        Course course = new Course();
        course.setId(5L);
        course.setTeacherId(99L);

        when(studentMapper.selectById(1L)).thenReturn(student);
        when(courseMapper.selectById(5L)).thenReturn(course);

        BusinessException exception = assertThrows(BusinessException.class, () -> scoreService.create(request));

        assertEquals(ErrorCode.FORBIDDEN, exception.getCode());
        assertEquals("只能维护本人授课课程的成绩", exception.getMessage());
        verify(scoreMapper, never()).insert(org.mockito.ArgumentMatchers.<Score>any());
    }

    @Test
    void studentCannotViewAnotherStudentsScore() {
        authenticate(RoleConstants.STUDENT, 7L);
        Score score = new Score();
        score.setId(11L);
        score.setStudentId(9L);
        score.setCourseId(3L);
        when(scoreMapper.selectById(11L)).thenReturn(score);

        BusinessException exception = assertThrows(BusinessException.class, () -> scoreService.getDetail(11L));

        assertEquals(ErrorCode.FORBIDDEN, exception.getCode());
        assertEquals("只能查看本人的成绩信息", exception.getMessage());
        verify(courseMapper, never()).selectById(anyLong());
    }
}

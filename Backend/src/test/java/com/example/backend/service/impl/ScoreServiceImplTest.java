package com.example.backend.service.impl;

import com.example.backend.common.constants.RoleConstants;
import com.example.backend.common.exception.BusinessException;
import com.example.backend.common.result.ErrorCode;
import com.example.backend.dto.request.ScoreRequest;
import com.example.backend.dto.response.ScoreImportResultVO;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;

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

    @Test
    void teacherCannotImportScoresForAnotherTeachersCourse() {
        authenticate(RoleConstants.TEACHER, 2L);
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "scores.csv",
                "text/csv",
                "studentNo,courseNo,score,semester\n2023001001,CS101,92,2024-2025-1\n".getBytes(StandardCharsets.UTF_8)
        );

        Student student = new Student();
        student.setId(1L);
        student.setStudentNo("2023001001");
        Course course = new Course();
        course.setId(3L);
        course.setCourseNo("CS101");
        course.setTeacherId(88L);

        when(studentMapper.selectOne(any())).thenReturn(student);
        when(courseMapper.selectOne(any())).thenReturn(course);

        ScoreImportResultVO result = scoreService.importScores(file);

        assertEquals(1, result.getTotalRows());
        assertEquals(0, result.getCreatedCount());
        assertEquals(0, result.getUpdatedCount());
        assertEquals(1, result.getSkippedCount());
        assertEquals("第2行: 只能维护本人授课课程的成绩", result.getErrorMessages().getFirst());
        verify(scoreMapper, never()).insert(any(Score.class));
    }

    @Test
    void importShouldUpdateExistingScore() {
        authenticate(RoleConstants.ADMIN, null);
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "scores.csv",
                "text/csv",
                "studentNo,courseNo,score,semester\n2023001001,CS101,98,2024-2025-1\n".getBytes(StandardCharsets.UTF_8)
        );

        Student student = new Student();
        student.setId(1L);
        student.setStudentNo("2023001001");
        Course course = new Course();
        course.setId(3L);
        course.setCourseNo("CS101");
        course.setTeacherId(2L);
        Score existing = new Score();
        existing.setId(11L);
        existing.setStudentId(1L);
        existing.setCourseId(3L);
        existing.setSemester("2024-2025-1");

        when(studentMapper.selectOne(any())).thenReturn(student);
        when(courseMapper.selectOne(any())).thenReturn(course);
        when(scoreMapper.selectOne(any())).thenReturn(existing);

        ScoreImportResultVO result = scoreService.importScores(file);

        assertEquals(1, result.getTotalRows());
        assertEquals(0, result.getCreatedCount());
        assertEquals(1, result.getUpdatedCount());
        assertEquals(0, result.getSkippedCount());
        verify(scoreMapper).updateById(existing);
    }
}

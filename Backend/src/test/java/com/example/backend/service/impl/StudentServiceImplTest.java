package com.example.backend.service.impl;

import com.example.backend.common.constants.RoleConstants;
import com.example.backend.common.exception.BusinessException;
import com.example.backend.common.result.ErrorCode;
import com.example.backend.entity.Student;
import com.example.backend.mapper.ClazzMapper;
import com.example.backend.mapper.StudentMapper;
import com.example.backend.support.SecurityTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentServiceImplTest extends SecurityTestSupport {

    @Mock
    private ClazzMapper clazzMapper;

    @Mock
    private StudentMapper studentMapper;

    private StudentServiceImpl studentService;

    @BeforeEach
    void setUp() {
        studentService = new StudentServiceImpl(clazzMapper);
        ReflectionTestUtils.setField(studentService, "baseMapper", studentMapper);
    }

    @Test
    void studentCannotViewAnotherStudentsDetail() {
        authenticate(RoleConstants.STUDENT, 7L);
        Student student = new Student();
        student.setId(42L);
        when(studentMapper.selectById(42L)).thenReturn(student);

        BusinessException exception = assertThrows(BusinessException.class, () -> studentService.getDetail(42L));

        assertEquals(ErrorCode.FORBIDDEN, exception.getCode());
        assertEquals("只能查看本人的学生信息", exception.getMessage());
    }
}

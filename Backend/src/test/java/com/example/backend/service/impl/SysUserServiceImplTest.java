package com.example.backend.service.impl;

import com.example.backend.common.constants.RoleConstants;
import com.example.backend.common.exception.BusinessException;
import com.example.backend.common.result.ErrorCode;
import com.example.backend.dto.request.UserCreateRequest;
import com.example.backend.dto.request.UserUpdateRequest;
import com.example.backend.entity.SysUser;
import com.example.backend.entity.Teacher;
import com.example.backend.mapper.StudentMapper;
import com.example.backend.mapper.SysUserMapper;
import com.example.backend.mapper.TeacherMapper;
import com.example.backend.support.SecurityTestSupport;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SysUserServiceImplTest extends SecurityTestSupport {

    @Mock
    private TeacherMapper teacherMapper;

    @Mock
    private StudentMapper studentMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SysUserMapper sysUserMapper;

    private SysUserServiceImpl sysUserService;

    @BeforeEach
    void setUp() {
        sysUserService = new SysUserServiceImpl(teacherMapper, studentMapper, passwordEncoder);
        ReflectionTestUtils.setField(sysUserService, "baseMapper", sysUserMapper);
    }

    @Test
    void createShouldRejectAlreadyBoundTeacherProfile() {
        UserCreateRequest request = new UserCreateRequest();
        request.setUsername("teacher-account");
        request.setPassword("secret123");
        request.setRole(RoleConstants.TEACHER);
        request.setRefId(8L);
        request.setStatus(1);

        Teacher teacher = new Teacher();
        teacher.setId(8L);
        when(sysUserMapper.selectCount(any())).thenReturn(0L, 1L);
        when(teacherMapper.selectById(8L)).thenReturn(teacher);

        BusinessException exception = assertThrows(BusinessException.class, () -> sysUserService.create(request));

        assertEquals(ErrorCode.CONFLICT, exception.getCode());
        assertEquals("该档案已绑定其他账号", exception.getMessage());
        verify(sysUserMapper, never()).insert(any(SysUser.class));
    }

    @Test
    void updateShouldRejectDisablingCurrentUser() {
        authenticate(5L, RoleConstants.ADMIN, null);
        SysUser user = new SysUser();
        user.setId(5L);
        user.setRole(RoleConstants.ADMIN);
        when(sysUserMapper.selectById(5L)).thenReturn(user);

        UserUpdateRequest request = new UserUpdateRequest();
        request.setUsername("admin");
        request.setRole(RoleConstants.ADMIN);
        request.setStatus(0);

        BusinessException exception = assertThrows(BusinessException.class, () -> sysUserService.update(5L, request));

        assertEquals(ErrorCode.FORBIDDEN, exception.getCode());
        assertEquals("不能禁用当前登录账号", exception.getMessage());
    }

    @Test
    void deleteShouldRejectCurrentUser() {
        authenticate(9L, RoleConstants.ADMIN, null);
        SysUser user = new SysUser();
        user.setId(9L);
        when(sysUserMapper.selectById(9L)).thenReturn(user);

        BusinessException exception = assertThrows(BusinessException.class, () -> sysUserService.delete(9L));

        assertEquals(ErrorCode.FORBIDDEN, exception.getCode());
        assertEquals("不能删除当前登录账号", exception.getMessage());
    }
}

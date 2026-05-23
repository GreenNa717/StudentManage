package com.example.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.common.constants.RoleConstants;
import com.example.backend.common.exception.BusinessException;
import com.example.backend.common.result.ErrorCode;
import com.example.backend.common.result.PageResult;
import com.example.backend.common.util.SecurityUtils;
import com.example.backend.dto.request.UserCreateRequest;
import com.example.backend.dto.request.UserUpdateRequest;
import com.example.backend.dto.response.UserVO;
import com.example.backend.entity.Student;
import com.example.backend.entity.SysUser;
import com.example.backend.entity.Teacher;
import com.example.backend.mapper.StudentMapper;
import com.example.backend.mapper.SysUserMapper;
import com.example.backend.mapper.TeacherMapper;
import com.example.backend.service.SysUserService;
import org.springframework.beans.BeanUtils;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SysUserServiceImpl extends ServiceImpl<SysUserMapper, SysUser> implements SysUserService {

    public static final String DEFAULT_PASSWORD = "123456";

    private final TeacherMapper teacherMapper;
    private final StudentMapper studentMapper;
    private final PasswordEncoder passwordEncoder;

    public SysUserServiceImpl(TeacherMapper teacherMapper, StudentMapper studentMapper, PasswordEncoder passwordEncoder) {
        this.teacherMapper = teacherMapper;
        this.studentMapper = studentMapper;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public PageResult<UserVO> page(Integer page, Integer size, String keyword, String role, Integer status) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(SysUser::getUsername, keyword);
        }
        if (role != null && !role.isBlank()) {
            wrapper.eq(SysUser::getRole, role);
        }
        if (status != null) {
            wrapper.eq(SysUser::getStatus, status);
        }
        wrapper.orderByDesc(SysUser::getCreateTime);

        Page<SysUser> pageResult = baseMapper.selectPage(new Page<>(page, size), wrapper);
        List<UserVO> records = pageResult.getRecords().stream().map(this::toVO).toList();
        return new PageResult<>(pageResult.getCurrent(), pageResult.getSize(), pageResult.getTotal(), records);
    }

    @Override
    public UserVO getDetail(Long id) {
        SysUser user = getRequiredUser(id);
        return toVO(user);
    }

    @Override
    public void create(UserCreateRequest request) {
        ensureUsernameUnique(request.getUsername(), null);
        Long refId = validateRoleAndRef(request.getRole(), request.getRefId(), null);

        SysUser user = new SysUser();
        user.setUsername(request.getUsername().trim());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(request.getRole());
        user.setRefId(refId);
        user.setStatus(request.getStatus());
        baseMapper.insert(user);
    }

    @Override
    public void update(Long id, UserUpdateRequest request) {
        SysUser user = getRequiredUser(id);
        ensureCurrentUserEditable(user, request);
        ensureUsernameUnique(request.getUsername(), id);
        Long refId = validateRoleAndRef(request.getRole(), request.getRefId(), id);

        user.setUsername(request.getUsername().trim());
        user.setRole(request.getRole());
        user.setRefId(refId);
        user.setStatus(request.getStatus());
        baseMapper.updateById(user);
    }

    @Override
    public void delete(Long id) {
        SysUser user = getRequiredUser(id);
        if (isCurrentUser(user.getId())) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "不能删除当前登录账号");
        }
        baseMapper.deleteById(id);
    }

    @Override
    public void resetPassword(Long id) {
        SysUser user = getRequiredUser(id);
        user.setPassword(passwordEncoder.encode(DEFAULT_PASSWORD));
        baseMapper.updateById(user);
    }

    private SysUser getRequiredUser(Long id) {
        SysUser user = baseMapper.selectById(id);
        if (user == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "用户不存在");
        }
        return user;
    }

    private void ensureUsernameUnique(String username, Long excludeId) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getUsername, username.trim());
        if (excludeId != null) {
            wrapper.ne(SysUser::getId, excludeId);
        }
        if (baseMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "用户名已存在");
        }
    }

    private Long validateRoleAndRef(String role, Long refId, Long excludeId) {
        String normalizedRole = role == null ? "" : role.trim();
        if (RoleConstants.ADMIN.equals(normalizedRole)) {
            return null;
        }
        if (RoleConstants.TEACHER.equals(normalizedRole)) {
            if (refId == null) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "教师账号必须关联教师档案");
            }
            Teacher teacher = teacherMapper.selectById(refId);
            if (teacher == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND, "关联教师不存在");
            }
            ensureRefUnused(normalizedRole, refId, excludeId);
            return refId;
        }
        if (RoleConstants.STUDENT.equals(normalizedRole)) {
            if (refId == null) {
                throw new BusinessException(ErrorCode.PARAM_ERROR, "学生账号必须关联学生档案");
            }
            Student student = studentMapper.selectById(refId);
            if (student == null) {
                throw new BusinessException(ErrorCode.NOT_FOUND, "关联学生不存在");
            }
            ensureRefUnused(normalizedRole, refId, excludeId);
            return refId;
        }
        throw new BusinessException(ErrorCode.PARAM_ERROR, "不支持的角色类型");
    }

    private void ensureRefUnused(String role, Long refId, Long excludeId) {
        LambdaQueryWrapper<SysUser> wrapper = new LambdaQueryWrapper<SysUser>()
                .eq(SysUser::getRole, role)
                .eq(SysUser::getRefId, refId);
        if (excludeId != null) {
            wrapper.ne(SysUser::getId, excludeId);
        }
        if (baseMapper.selectCount(wrapper) > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "该档案已绑定其他账号");
        }
    }

    private void ensureCurrentUserEditable(SysUser user, UserUpdateRequest request) {
        if (!isCurrentUser(user.getId())) {
            return;
        }
        if (request.getStatus() != null && request.getStatus() == 0) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "不能禁用当前登录账号");
        }
    }

    private boolean isCurrentUser(Long userId) {
        return SecurityUtils.getLoginUser().getId().equals(userId);
    }

    private UserVO toVO(SysUser user) {
        UserVO vo = new UserVO();
        BeanUtils.copyProperties(user, vo);
        vo.setRefName(resolveRefName(user));
        return vo;
    }

    private String resolveRefName(SysUser user) {
        if (RoleConstants.TEACHER.equals(user.getRole()) && user.getRefId() != null) {
            Teacher teacher = teacherMapper.selectById(user.getRefId());
            if (teacher != null) {
                return teacher.getName() + "(" + teacher.getTeacherNo() + ")";
            }
        }
        if (RoleConstants.STUDENT.equals(user.getRole()) && user.getRefId() != null) {
            Student student = studentMapper.selectById(user.getRefId());
            if (student != null) {
                return student.getName() + "(" + student.getStudentNo() + ")";
            }
        }
        return "-";
    }
}

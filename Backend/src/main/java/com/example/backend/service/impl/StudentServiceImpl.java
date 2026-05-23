package com.example.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.common.exception.BusinessException;
import com.example.backend.common.constants.RoleConstants;
import com.example.backend.common.result.ErrorCode;
import com.example.backend.common.result.PageResult;
import com.example.backend.common.util.SecurityUtils;
import com.example.backend.dto.request.StudentRequest;
import com.example.backend.dto.response.StudentVO;
import com.example.backend.entity.Clazz;
import com.example.backend.entity.Student;
import com.example.backend.mapper.ClazzMapper;
import com.example.backend.mapper.StudentMapper;
import com.example.backend.service.StudentService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StudentServiceImpl extends ServiceImpl<StudentMapper, Student> implements StudentService {

    private final ClazzMapper clazzMapper;

    public StudentServiceImpl(ClazzMapper clazzMapper) {
        this.clazzMapper = clazzMapper;
    }

    @Override
    public PageResult<StudentVO> page(Integer page, Integer size, String keyword, Long classId) {
        LambdaQueryWrapper<Student> wrapper = new LambdaQueryWrapper<>();
        applyDataScope(wrapper);
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(Student::getName, keyword).or().like(Student::getStudentNo, keyword));
        }
        if (classId != null) {
            wrapper.eq(Student::getClassId, classId);
        }
        wrapper.orderByDesc(Student::getCreateTime);

        Page<Student> pageResult = baseMapper.selectPage(new Page<>(page, size), wrapper);

        List<StudentVO> records = pageResult.getRecords().stream().map(this::toVO).toList();
        return new PageResult<>(pageResult.getCurrent(), pageResult.getSize(), pageResult.getTotal(), records);
    }

    @Override
    public StudentVO getDetail(Long id) {
        Student student = baseMapper.selectById(id);
        if (student == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "学生不存在");
        }
        ensureReadable(student);
        return toVO(student);
    }

    @Override
    public void create(StudentRequest request) {
        checkClass(request.getClassId());
        Long count = baseMapper.selectCount(
                new LambdaQueryWrapper<Student>().eq(Student::getStudentNo, request.getStudentNo()));
        if (count > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "学号已存在");
        }
        Student student = new Student();
        BeanUtils.copyProperties(request, student);
        baseMapper.insert(student);
    }

    @Override
    public void update(Long id, StudentRequest request) {
        Student student = baseMapper.selectById(id);
        if (student == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "学生不存在");
        }
        checkClass(request.getClassId());
        Long count = baseMapper.selectCount(
                new LambdaQueryWrapper<Student>().eq(Student::getStudentNo, request.getStudentNo()).ne(Student::getId, id));
        if (count > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "学号已存在");
        }
        BeanUtils.copyProperties(request, student);
        student.setId(id);
        baseMapper.updateById(student);
    }

    @Override
    public void delete(Long id) {
        Student student = baseMapper.selectById(id);
        if (student == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "学生不存在");
        }
        baseMapper.deleteById(id);
    }

    private void checkClass(Long classId) {
        Clazz clazz = clazzMapper.selectById(classId);
        if (clazz == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "所属班级不存在");
        }
    }

    private void applyDataScope(LambdaQueryWrapper<Student> wrapper) {
        var loginUser = SecurityUtils.getLoginUser();
        if (RoleConstants.STUDENT.equals(loginUser.getRole())) {
            if (loginUser.getRefId() == null) {
                wrapper.eq(Student::getId, -1L);
                return;
            }
            wrapper.eq(Student::getId, loginUser.getRefId());
        }
    }

    private void ensureReadable(Student student) {
        var loginUser = SecurityUtils.getLoginUser();
        if (RoleConstants.STUDENT.equals(loginUser.getRole())
                && (loginUser.getRefId() == null || !student.getId().equals(loginUser.getRefId()))) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "只能查看本人的学生信息");
        }
    }

    private StudentVO toVO(Student student) {
        StudentVO vo = new StudentVO();
        BeanUtils.copyProperties(student, vo);
        Clazz clazz = clazzMapper.selectById(student.getClassId());
        if (clazz != null) {
            vo.setClassName(clazz.getName());
        }
        return vo;
    }
}

package com.example.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.common.exception.BusinessException;
import com.example.backend.common.result.ErrorCode;
import com.example.backend.common.result.PageResult;
import com.example.backend.dto.request.TeacherRequest;
import com.example.backend.dto.response.TeacherVO;
import com.example.backend.entity.Course;
import com.example.backend.entity.Department;
import com.example.backend.entity.Teacher;
import com.example.backend.mapper.CourseMapper;
import com.example.backend.mapper.DepartmentMapper;
import com.example.backend.mapper.TeacherMapper;
import com.example.backend.service.TeacherService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeacherServiceImpl extends ServiceImpl<TeacherMapper, Teacher> implements TeacherService {

    private final DepartmentMapper departmentMapper;
    private final CourseMapper courseMapper;

    public TeacherServiceImpl(DepartmentMapper departmentMapper, CourseMapper courseMapper) {
        this.departmentMapper = departmentMapper;
        this.courseMapper = courseMapper;
    }

    @Override
    public PageResult<TeacherVO> page(Integer page, Integer size, String keyword, Long departmentId) {
        LambdaQueryWrapper<Teacher> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.and(w -> w.like(Teacher::getName, keyword).or().like(Teacher::getTeacherNo, keyword));
        }
        if (departmentId != null) {
            wrapper.eq(Teacher::getDepartmentId, departmentId);
        }
        wrapper.orderByDesc(Teacher::getCreateTime);

        Page<Teacher> pageResult = baseMapper.selectPage(new Page<>(page, size), wrapper);

        List<TeacherVO> records = pageResult.getRecords().stream().map(this::toVO).toList();
        return new PageResult<>(pageResult.getCurrent(), pageResult.getSize(), pageResult.getTotal(), records);
    }

    @Override
    public TeacherVO getDetail(Long id) {
        Teacher teacher = baseMapper.selectById(id);
        if (teacher == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "教师不存在");
        }
        return toVO(teacher);
    }

    @Override
    public void create(TeacherRequest request) {
        checkDepartment(request.getDepartmentId());
        Long count = baseMapper.selectCount(
                new LambdaQueryWrapper<Teacher>().eq(Teacher::getTeacherNo, request.getTeacherNo()));
        if (count > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "工号已存在");
        }
        Teacher teacher = new Teacher();
        BeanUtils.copyProperties(request, teacher);
        baseMapper.insert(teacher);
    }

    @Override
    public void update(Long id, TeacherRequest request) {
        Teacher teacher = baseMapper.selectById(id);
        if (teacher == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "教师不存在");
        }
        checkDepartment(request.getDepartmentId());
        Long count = baseMapper.selectCount(
                new LambdaQueryWrapper<Teacher>().eq(Teacher::getTeacherNo, request.getTeacherNo()).ne(Teacher::getId, id));
        if (count > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "工号已存在");
        }
        BeanUtils.copyProperties(request, teacher);
        teacher.setId(id);
        baseMapper.updateById(teacher);
    }

    @Override
    public void delete(Long id) {
        Teacher teacher = baseMapper.selectById(id);
        if (teacher == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "教师不存在");
        }
        Long courseCount = courseMapper.selectCount(
                new LambdaQueryWrapper<Course>().eq(Course::getTeacherId, id));
        if (courseCount > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "该教师下存在课程，不可删除");
        }
        baseMapper.deleteById(id);
    }

    private void checkDepartment(Long departmentId) {
        Department dept = departmentMapper.selectById(departmentId);
        if (dept == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "所属院系不存在");
        }
    }

    private TeacherVO toVO(Teacher teacher) {
        TeacherVO vo = new TeacherVO();
        BeanUtils.copyProperties(teacher, vo);
        Department dept = departmentMapper.selectById(teacher.getDepartmentId());
        if (dept != null) {
            vo.setDepartmentName(dept.getName());
        }
        return vo;
    }
}

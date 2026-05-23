package com.example.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.common.exception.BusinessException;
import com.example.backend.common.result.ErrorCode;
import com.example.backend.common.result.PageResult;
import com.example.backend.dto.request.DepartmentRequest;
import com.example.backend.dto.response.DepartmentVO;
import com.example.backend.entity.Department;
import com.example.backend.entity.Major;
import com.example.backend.entity.Teacher;
import com.example.backend.mapper.DepartmentMapper;
import com.example.backend.mapper.MajorMapper;
import com.example.backend.mapper.TeacherMapper;
import com.example.backend.service.DepartmentService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class DepartmentServiceImpl extends ServiceImpl<DepartmentMapper, Department> implements DepartmentService {

    private final MajorMapper majorMapper;
    private final TeacherMapper teacherMapper;

    public DepartmentServiceImpl(MajorMapper majorMapper, TeacherMapper teacherMapper) {
        this.majorMapper = majorMapper;
        this.teacherMapper = teacherMapper;
    }

    @Override
    public PageResult<DepartmentVO> page(Integer page, Integer size, String keyword) {
        LambdaQueryWrapper<Department> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(Department::getName, keyword);
        }
        wrapper.orderByDesc(Department::getCreateTime);

        Page<Department> pageResult = baseMapper.selectPage(new Page<>(page, size), wrapper);

        List<DepartmentVO> records = pageResult.getRecords().stream().map(this::toVO).toList();
        return new PageResult<>(pageResult.getCurrent(), pageResult.getSize(), pageResult.getTotal(), records);
    }

    @Override
    public DepartmentVO getDetail(Long id) {
        Department dept = baseMapper.selectById(id);
        if (dept == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "院系不存在");
        }
        return toVO(dept);
    }

    @Override
    public void create(DepartmentRequest request) {
        Department dept = new Department();
        BeanUtils.copyProperties(request, dept);
        baseMapper.insert(dept);
    }

    @Override
    public void update(Long id, DepartmentRequest request) {
        Department dept = baseMapper.selectById(id);
        if (dept == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "院系不存在");
        }
        BeanUtils.copyProperties(request, dept);
        dept.setId(id);
        baseMapper.updateById(dept);
    }

    @Override
    public void delete(Long id) {
        Department dept = baseMapper.selectById(id);
        if (dept == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "院系不存在");
        }
        Long majorCount = majorMapper.selectCount(
                new LambdaQueryWrapper<Major>().eq(Major::getDepartmentId, id));
        if (majorCount > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "该院系下存在专业，不可删除");
        }
        Long teacherCount = teacherMapper.selectCount(
                new LambdaQueryWrapper<Teacher>().eq(Teacher::getDepartmentId, id));
        if (teacherCount > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "该院系下存在教师，不可删除");
        }
        baseMapper.deleteById(id);
    }

    private DepartmentVO toVO(Department dept) {
        DepartmentVO vo = new DepartmentVO();
        BeanUtils.copyProperties(dept, vo);
        return vo;
    }
}

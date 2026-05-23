package com.example.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.common.exception.BusinessException;
import com.example.backend.common.result.ErrorCode;
import com.example.backend.common.result.PageResult;
import com.example.backend.dto.request.MajorRequest;
import com.example.backend.dto.response.MajorVO;
import com.example.backend.entity.Clazz;
import com.example.backend.entity.Department;
import com.example.backend.entity.Major;
import com.example.backend.mapper.ClazzMapper;
import com.example.backend.mapper.DepartmentMapper;
import com.example.backend.mapper.MajorMapper;
import com.example.backend.service.MajorService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class MajorServiceImpl extends ServiceImpl<MajorMapper, Major> implements MajorService {

    private final DepartmentMapper departmentMapper;
    private final ClazzMapper clazzMapper;

    public MajorServiceImpl(DepartmentMapper departmentMapper, ClazzMapper clazzMapper) {
        this.departmentMapper = departmentMapper;
        this.clazzMapper = clazzMapper;
    }

    @Override
    public PageResult<MajorVO> page(Integer page, Integer size, String keyword, Long departmentId) {
        LambdaQueryWrapper<Major> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(Major::getName, keyword);
        }
        if (departmentId != null) {
            wrapper.eq(Major::getDepartmentId, departmentId);
        }
        wrapper.orderByDesc(Major::getCreateTime);

        Page<Major> pageResult = baseMapper.selectPage(new Page<>(page, size), wrapper);

        List<MajorVO> records = pageResult.getRecords().stream().map(this::toVO).toList();
        return new PageResult<>(pageResult.getCurrent(), pageResult.getSize(), pageResult.getTotal(), records);
    }

    @Override
    public MajorVO getDetail(Long id) {
        Major major = baseMapper.selectById(id);
        if (major == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "专业不存在");
        }
        return toVO(major);
    }

    @Override
    public void create(MajorRequest request) {
        checkDepartment(request.getDepartmentId());
        Major major = new Major();
        BeanUtils.copyProperties(request, major);
        baseMapper.insert(major);
    }

    @Override
    public void update(Long id, MajorRequest request) {
        Major major = baseMapper.selectById(id);
        if (major == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "专业不存在");
        }
        checkDepartment(request.getDepartmentId());
        BeanUtils.copyProperties(request, major);
        major.setId(id);
        baseMapper.updateById(major);
    }

    @Override
    public void delete(Long id) {
        Major major = baseMapper.selectById(id);
        if (major == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "专业不存在");
        }
        Long clazzCount = clazzMapper.selectCount(
                new LambdaQueryWrapper<Clazz>().eq(Clazz::getMajorId, id));
        if (clazzCount > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "该专业下存在班级，不可删除");
        }
        baseMapper.deleteById(id);
    }

    private void checkDepartment(Long departmentId) {
        Department dept = departmentMapper.selectById(departmentId);
        if (dept == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "所属院系不存在");
        }
    }

    private MajorVO toVO(Major major) {
        MajorVO vo = new MajorVO();
        BeanUtils.copyProperties(major, vo);
        Department dept = departmentMapper.selectById(major.getDepartmentId());
        if (dept != null) {
            vo.setDepartmentName(dept.getName());
        }
        return vo;
    }
}

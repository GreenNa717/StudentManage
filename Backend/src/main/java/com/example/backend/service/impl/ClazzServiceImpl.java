package com.example.backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.backend.common.exception.BusinessException;
import com.example.backend.common.result.ErrorCode;
import com.example.backend.common.result.PageResult;
import com.example.backend.dto.request.ClazzRequest;
import com.example.backend.dto.response.ClazzVO;
import com.example.backend.entity.Clazz;
import com.example.backend.entity.Major;
import com.example.backend.entity.Student;
import com.example.backend.mapper.ClazzMapper;
import com.example.backend.mapper.MajorMapper;
import com.example.backend.mapper.StudentMapper;
import com.example.backend.service.ClazzService;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ClazzServiceImpl extends ServiceImpl<ClazzMapper, Clazz> implements ClazzService {

    private final MajorMapper majorMapper;
    private final StudentMapper studentMapper;

    public ClazzServiceImpl(MajorMapper majorMapper, StudentMapper studentMapper) {
        this.majorMapper = majorMapper;
        this.studentMapper = studentMapper;
    }

    @Override
    public PageResult<ClazzVO> page(Integer page, Integer size, String keyword, Long majorId, Integer grade) {
        LambdaQueryWrapper<Clazz> wrapper = new LambdaQueryWrapper<>();
        if (keyword != null && !keyword.isBlank()) {
            wrapper.like(Clazz::getName, keyword);
        }
        if (majorId != null) {
            wrapper.eq(Clazz::getMajorId, majorId);
        }
        if (grade != null) {
            wrapper.eq(Clazz::getGrade, grade);
        }
        wrapper.orderByDesc(Clazz::getCreateTime);

        Page<Clazz> pageResult = baseMapper.selectPage(new Page<>(page, size), wrapper);

        List<ClazzVO> records = pageResult.getRecords().stream().map(this::toVO).toList();
        return new PageResult<>(pageResult.getCurrent(), pageResult.getSize(), pageResult.getTotal(), records);
    }

    @Override
    public ClazzVO getDetail(Long id) {
        Clazz clazz = baseMapper.selectById(id);
        if (clazz == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "班级不存在");
        }
        return toVO(clazz);
    }

    @Override
    public void create(ClazzRequest request) {
        checkMajor(request.getMajorId());
        Clazz clazz = new Clazz();
        BeanUtils.copyProperties(request, clazz);
        baseMapper.insert(clazz);
    }

    @Override
    public void update(Long id, ClazzRequest request) {
        Clazz clazz = baseMapper.selectById(id);
        if (clazz == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "班级不存在");
        }
        checkMajor(request.getMajorId());
        BeanUtils.copyProperties(request, clazz);
        clazz.setId(id);
        baseMapper.updateById(clazz);
    }

    @Override
    public void delete(Long id) {
        Clazz clazz = baseMapper.selectById(id);
        if (clazz == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "班级不存在");
        }
        Long studentCount = studentMapper.selectCount(
                new LambdaQueryWrapper<Student>().eq(Student::getClassId, id));
        if (studentCount > 0) {
            throw new BusinessException(ErrorCode.CONFLICT, "该班级下存在学生，不可删除");
        }
        baseMapper.deleteById(id);
    }

    private void checkMajor(Long majorId) {
        Major major = majorMapper.selectById(majorId);
        if (major == null) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "所属专业不存在");
        }
    }

    private ClazzVO toVO(Clazz clazz) {
        ClazzVO vo = new ClazzVO();
        BeanUtils.copyProperties(clazz, vo);
        Major major = majorMapper.selectById(clazz.getMajorId());
        if (major != null) {
            vo.setMajorName(major.getName());
        }
        return vo;
    }
}

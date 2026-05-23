package com.example.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.backend.common.result.PageResult;
import com.example.backend.dto.request.DepartmentRequest;
import com.example.backend.dto.response.DepartmentVO;
import com.example.backend.entity.Department;

public interface DepartmentService extends IService<Department> {

    PageResult<DepartmentVO> page(Integer page, Integer size, String keyword);

    DepartmentVO getDetail(Long id);

    void create(DepartmentRequest request);

    void update(Long id, DepartmentRequest request);

    void delete(Long id);
}

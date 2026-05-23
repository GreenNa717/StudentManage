package com.example.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.backend.common.result.PageResult;
import com.example.backend.dto.request.MajorRequest;
import com.example.backend.dto.response.MajorVO;
import com.example.backend.entity.Major;

public interface MajorService extends IService<Major> {

    PageResult<MajorVO> page(Integer page, Integer size, String keyword, Long departmentId);

    MajorVO getDetail(Long id);

    void create(MajorRequest request);

    void update(Long id, MajorRequest request);

    void delete(Long id);
}

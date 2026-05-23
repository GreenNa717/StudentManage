package com.example.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.backend.common.result.PageResult;
import com.example.backend.dto.request.ClazzRequest;
import com.example.backend.dto.response.ClazzVO;
import com.example.backend.entity.Clazz;

public interface ClazzService extends IService<Clazz> {

    PageResult<ClazzVO> page(Integer page, Integer size, String keyword, Long majorId, Integer grade);

    ClazzVO getDetail(Long id);

    void create(ClazzRequest request);

    void update(Long id, ClazzRequest request);

    void delete(Long id);
}

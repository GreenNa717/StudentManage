package com.example.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.backend.common.result.PageResult;
import com.example.backend.dto.request.TeacherRequest;
import com.example.backend.dto.response.TeacherVO;
import com.example.backend.entity.Teacher;

public interface TeacherService extends IService<Teacher> {

    PageResult<TeacherVO> page(Integer page, Integer size, String keyword, Long departmentId);

    TeacherVO getDetail(Long id);

    void create(TeacherRequest request);

    void update(Long id, TeacherRequest request);

    void delete(Long id);
}

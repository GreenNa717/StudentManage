package com.example.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.backend.common.result.PageResult;
import com.example.backend.dto.request.StudentRequest;
import com.example.backend.dto.response.StudentVO;
import com.example.backend.entity.Student;

public interface StudentService extends IService<Student> {

    PageResult<StudentVO> page(Integer page, Integer size, String keyword, Long classId);

    StudentVO getDetail(Long id);

    void create(StudentRequest request);

    void update(Long id, StudentRequest request);

    void delete(Long id);
}

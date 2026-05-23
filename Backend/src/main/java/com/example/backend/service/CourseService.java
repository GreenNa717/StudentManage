package com.example.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.backend.common.result.PageResult;
import com.example.backend.dto.request.CourseRequest;
import com.example.backend.dto.response.CourseVO;
import com.example.backend.entity.Course;

public interface CourseService extends IService<Course> {

    PageResult<CourseVO> page(Integer page, Integer size, String keyword, Long teacherId);

    CourseVO getDetail(Long id);

    void create(CourseRequest request);

    void update(Long id, CourseRequest request);

    void delete(Long id);
}

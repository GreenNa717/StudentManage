package com.example.backend.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class CourseVO {

    private Long id;
    private String courseNo;
    private String name;
    private BigDecimal credit;
    private Integer hours;
    private Long teacherId;
    private String teacherName;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

package com.example.backend.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class MajorVO {

    private Long id;
    private String name;
    private Long departmentId;
    private String departmentName;
    private Integer duration;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

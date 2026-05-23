package com.example.backend.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DepartmentVO {

    private Long id;
    private String name;
    private String dean;
    private String phone;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

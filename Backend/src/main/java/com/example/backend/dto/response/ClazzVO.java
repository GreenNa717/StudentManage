package com.example.backend.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class ClazzVO {

    private Long id;
    private String name;
    private Long majorId;
    private String majorName;
    private Integer grade;
    private String advisor;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

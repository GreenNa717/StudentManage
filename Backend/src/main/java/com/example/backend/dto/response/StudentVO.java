package com.example.backend.dto.response;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
public class StudentVO {

    private Long id;
    private String studentNo;
    private String name;
    private Integer gender;
    private LocalDate birthDate;
    private Long classId;
    private String className;
    private String phone;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

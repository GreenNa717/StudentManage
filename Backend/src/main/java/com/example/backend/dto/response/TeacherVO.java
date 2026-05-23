package com.example.backend.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class TeacherVO {

    private Long id;
    private String teacherNo;
    private String name;
    private Integer gender;
    private Long departmentId;
    private String departmentName;
    private String title;
    private String phone;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

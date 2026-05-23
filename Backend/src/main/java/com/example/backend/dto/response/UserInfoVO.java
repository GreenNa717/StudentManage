package com.example.backend.dto.response;

import lombok.Data;

@Data
public class UserInfoVO {

    private Long id;
    private String username;
    private String role;
    private Long refId;
    private String name;
    private String phone;
    private String departmentName;
    private String className;
    private String studentNo;
    private String teacherNo;
    private String title;
    private Integer gender;
    private String lastLoginTime;
}

package com.example.backend.dto.response;

import lombok.Data;

@Data
public class DepartmentStudentCountVO {

    private String departmentName;
    private Long studentCount;
}

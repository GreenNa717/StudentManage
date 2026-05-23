package com.example.backend.dto.response;

import lombok.Data;

@Data
public class DashboardOverviewVO {

    private Long studentCount;
    private Long teacherCount;
    private Long courseCount;
    private Long departmentCount;
}

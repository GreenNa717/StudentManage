package com.example.backend.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class CourseRequest {

    @NotBlank(message = "课程号不能为空")
    @Size(max = 20)
    private String courseNo;

    @NotBlank(message = "课程名称不能为空")
    @Size(max = 50)
    private String name;

    @NotNull(message = "学分不能为空")
    private BigDecimal credit;

    @NotNull(message = "学时不能为空")
    private Integer hours;

    @NotNull(message = "授课教师不能为空")
    private Long teacherId;
}

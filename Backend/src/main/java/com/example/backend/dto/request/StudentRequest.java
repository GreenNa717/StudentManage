package com.example.backend.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class StudentRequest {

    @NotBlank(message = "学号不能为空")
    @Size(max = 20)
    private String studentNo;

    @NotBlank(message = "姓名不能为空")
    @Size(max = 20)
    private String name;

    @NotNull(message = "性别不能为空")
    private Integer gender;

    private LocalDate birthDate;

    @NotNull(message = "所属班级不能为空")
    private Long classId;

    @Size(max = 20)
    private String phone;
}

package com.example.backend.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class TeacherRequest {

    @NotBlank(message = "工号不能为空")
    @Size(max = 20)
    private String teacherNo;

    @NotBlank(message = "姓名不能为空")
    @Size(max = 20)
    private String name;

    @NotNull(message = "性别不能为空")
    private Integer gender;

    @NotNull(message = "所属院系不能为空")
    private Long departmentId;

    @Size(max = 20)
    private String title;

    @Size(max = 20)
    private String phone;
}

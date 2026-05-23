package com.example.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class ClazzRequest {

    @NotBlank(message = "班级名称不能为空")
    @Size(max = 30, message = "班级名称不超过30个字符")
    private String name;

    @NotNull(message = "所属专业不能为空")
    private Long majorId;

    @NotNull(message = "年级不能为空")
    private Integer grade;

    @Size(max = 20, message = "班主任姓名不超过20个字符")
    private String advisor;
}

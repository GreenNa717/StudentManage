package com.example.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class MajorRequest {

    @NotBlank(message = "专业名称不能为空")
    @Size(max = 50, message = "专业名称不超过50个字符")
    private String name;

    @NotNull(message = "所属院系不能为空")
    private Long departmentId;

    @NotNull(message = "学制不能为空")
    private Integer duration;
}

package com.example.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class DepartmentRequest {

    @NotBlank(message = "院系名称不能为空")
    @Size(max = 50, message = "院系名称不超过50个字符")
    private String name;

    @Size(max = 20, message = "院长姓名不超过20个字符")
    private String dean;

    @Size(max = 20, message = "联系电话不超过20个字符")
    private String phone;
}

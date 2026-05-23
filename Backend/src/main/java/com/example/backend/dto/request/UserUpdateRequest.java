package com.example.backend.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UserUpdateRequest {

    @NotBlank(message = "用户名不能为空")
    @Size(max = 30, message = "用户名长度不能超过30位")
    private String username;

    @NotBlank(message = "角色不能为空")
    private String role;

    private Long refId;

    @NotNull(message = "状态不能为空")
    private Integer status;
}

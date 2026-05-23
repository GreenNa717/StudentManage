package com.example.backend.dto.response;

import lombok.Data;

@Data
public class LoginVO {

    private String token;
    private String username;
    private String role;
    private Long refId;
}

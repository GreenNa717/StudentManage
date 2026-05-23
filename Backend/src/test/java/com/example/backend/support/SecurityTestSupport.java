package com.example.backend.support;

import com.example.backend.security.LoginUser;
import org.junit.jupiter.api.AfterEach;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

public abstract class SecurityTestSupport {

    @AfterEach
    void clearSecurityContext() {
        SecurityContextHolder.clearContext();
    }

    protected void authenticate(String role, Long refId) {
        authenticate(1L, role, refId);
    }

    protected void authenticate(Long id, String role, Long refId) {
        LoginUser loginUser = new LoginUser(id, "tester", "password", role, refId, true);
        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                loginUser, null, loginUser.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}

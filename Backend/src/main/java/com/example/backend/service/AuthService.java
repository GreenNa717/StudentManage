package com.example.backend.service;

import com.example.backend.dto.request.ChangePasswordRequest;
import com.example.backend.dto.request.LoginRequest;
import com.example.backend.dto.response.DashboardOverviewVO;
import com.example.backend.dto.response.DepartmentStudentCountVO;
import com.example.backend.dto.response.LoginVO;
import com.example.backend.dto.response.ScoreDistributionVO;
import com.example.backend.dto.response.UserInfoVO;

import java.util.List;

public interface AuthService {

    LoginVO login(LoginRequest request);

    UserInfoVO getCurrentUser();

    void changePassword(ChangePasswordRequest request);

    DashboardOverviewVO getDashboardOverview();

    List<DepartmentStudentCountVO> getDepartmentStudentCount();

    List<ScoreDistributionVO> getScoreDistribution(String semester);
}

package com.example.backend.controller;

import com.example.backend.common.result.Result;
import com.example.backend.dto.response.DashboardOverviewVO;
import com.example.backend.dto.response.DepartmentStudentCountVO;
import com.example.backend.dto.response.ScoreDistributionVO;
import com.example.backend.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
@RequiredArgsConstructor
public class DashboardController {

    private final AuthService authService;

    @GetMapping("/overview")
    public Result<DashboardOverviewVO> overview() {
        return Result.success(authService.getDashboardOverview());
    }

    @GetMapping("/department-students")
    public Result<List<DepartmentStudentCountVO>> departmentStudents() {
        return Result.success(authService.getDepartmentStudentCount());
    }

    @GetMapping("/score-distribution")
    public Result<List<ScoreDistributionVO>> scoreDistribution(
            @RequestParam(required = false) String semester) {
        return Result.success(authService.getScoreDistribution(semester));
    }
}

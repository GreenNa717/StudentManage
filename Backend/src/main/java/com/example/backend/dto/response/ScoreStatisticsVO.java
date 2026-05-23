package com.example.backend.dto.response;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class ScoreStatisticsVO {

    private Long courseId;
    private String courseName;
    private Long totalCount;
    private BigDecimal avgScore;
    private BigDecimal maxScore;
    private BigDecimal minScore;
    private BigDecimal passRate;
}

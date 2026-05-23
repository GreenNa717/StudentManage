package com.example.backend.dto.response;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ScoreVO {

    private Long id;
    private Long studentId;
    private String studentName;
    private String studentNo;
    private Long courseId;
    private String courseName;
    private BigDecimal score;
    private String semester;
    private LocalDateTime createTime;
    private LocalDateTime updateTime;
}

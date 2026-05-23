package com.example.backend.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ScoreRequest {

    @NotNull(message = "学生不能为空")
    private Long studentId;

    @NotNull(message = "课程不能为空")
    private Long courseId;

    @NotNull(message = "成绩不能为空")
    @DecimalMin(value = "0", message = "成绩不能小于0")
    @DecimalMax(value = "100", message = "成绩不能大于100")
    private BigDecimal score;

    @NotBlank(message = "学期不能为空")
    @Size(max = 20)
    private String semester;
}

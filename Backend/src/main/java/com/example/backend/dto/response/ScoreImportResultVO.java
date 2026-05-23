package com.example.backend.dto.response;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class ScoreImportResultVO {

    private int totalRows;
    private int createdCount;
    private int updatedCount;
    private int skippedCount;
    private List<String> errorMessages = new ArrayList<>();
}

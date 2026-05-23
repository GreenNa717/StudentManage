package com.example.backend.controller;

import com.example.backend.common.result.PageResult;
import com.example.backend.common.result.Result;
import com.example.backend.dto.request.ScoreRequest;
import com.example.backend.dto.response.ScoreImportResultVO;
import com.example.backend.dto.response.ScoreStatisticsVO;
import com.example.backend.dto.response.ScoreVO;
import com.example.backend.service.ScoreService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.List;

@RestController
@RequestMapping("/api/scores")
@RequiredArgsConstructor
public class ScoreController {

    private final ScoreService scoreService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public Result<PageResult<ScoreVO>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) String semester) {
        return Result.success(scoreService.page(page, size, studentId, courseId, semester));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public Result<ScoreVO> getDetail(@PathVariable Long id) {
        return Result.success(scoreService.getDetail(id));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public Result<Void> create(@Valid @RequestBody ScoreRequest request) {
        scoreService.create(request);
        return Result.success();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ScoreRequest request) {
        scoreService.update(id, request);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        scoreService.delete(id);
        return Result.success();
    }

    @GetMapping("/statistics")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER', 'STUDENT')")
    public Result<List<ScoreStatisticsVO>> getStatistics(
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) String semester) {
        return Result.success(scoreService.getStatistics(courseId, semester));
    }

    @PostMapping("/import")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public Result<ScoreImportResultVO> importScores(@RequestParam("file") MultipartFile file) {
        return Result.success(scoreService.importScores(file));
    }

    @GetMapping("/export")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public org.springframework.http.ResponseEntity<byte[]> exportScores(
            @RequestParam(required = false) Long studentId,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) String semester) {
        ByteArrayInputStream stream = scoreService.exportScores(studentId, courseId, semester);
        return org.springframework.http.ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=scores.csv")
                .header(org.springframework.http.HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8")
                .body(stream.readAllBytes());
    }

    @GetMapping("/template")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public org.springframework.http.ResponseEntity<byte[]> exportTemplate() {
        ByteArrayInputStream stream = scoreService.exportTemplate();
        return org.springframework.http.ResponseEntity.ok()
                .header(org.springframework.http.HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=score-import-template.csv")
                .header(org.springframework.http.HttpHeaders.CONTENT_TYPE, "text/csv; charset=UTF-8")
                .body(stream.readAllBytes());
    }
}

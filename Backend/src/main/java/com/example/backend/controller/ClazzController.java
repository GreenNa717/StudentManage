package com.example.backend.controller;

import com.example.backend.common.result.PageResult;
import com.example.backend.common.result.Result;
import com.example.backend.dto.request.ClazzRequest;
import com.example.backend.dto.response.ClazzVO;
import com.example.backend.service.ClazzService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/classes")
@RequiredArgsConstructor
public class ClazzController {

    private final ClazzService clazzService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public Result<PageResult<ClazzVO>> page(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long majorId,
            @RequestParam(required = false) Integer grade) {
        return Result.success(clazzService.page(page, size, keyword, majorId, grade));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TEACHER')")
    public Result<ClazzVO> getDetail(@PathVariable Long id) {
        return Result.success(clazzService.getDetail(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> create(@Valid @RequestBody ClazzRequest request) {
        clazzService.create(request);
        return Result.success();
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> update(@PathVariable Long id, @Valid @RequestBody ClazzRequest request) {
        clazzService.update(id, request);
        return Result.success();
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public Result<Void> delete(@PathVariable Long id) {
        clazzService.delete(id);
        return Result.success();
    }
}

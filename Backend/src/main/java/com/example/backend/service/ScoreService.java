package com.example.backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.backend.common.result.PageResult;
import com.example.backend.dto.request.ScoreRequest;
import com.example.backend.dto.response.ScoreImportResultVO;
import com.example.backend.dto.response.ScoreStatisticsVO;
import com.example.backend.dto.response.ScoreVO;
import com.example.backend.entity.Score;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.util.List;

public interface ScoreService extends IService<Score> {

    PageResult<ScoreVO> page(Integer page, Integer size, Long studentId, Long courseId, String semester);

    ScoreVO getDetail(Long id);

    void create(ScoreRequest request);

    void update(Long id, ScoreRequest request);

    void delete(Long id);

    List<ScoreStatisticsVO> getStatistics(Long courseId, String semester);

    ScoreImportResultVO importScores(MultipartFile file);

    ByteArrayInputStream exportScores(Long studentId, Long courseId, String semester);

    ByteArrayInputStream exportTemplate();
}

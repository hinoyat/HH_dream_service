package com.c202.diary.emotion.controller;

import com.c202.diary.emotion.model.response.EmotionResponseDto;
import com.c202.diary.emotion.model.response.EmotionStatisticsResponseDto;
import com.c202.diary.emotion.service.EmotionService;
import com.c202.dto.ResponseDto;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/diaries/emotions")
@RequiredArgsConstructor
public class EmotionController {

    private final EmotionService emotionService;

    @GetMapping("")
    public ResponseEntity<ResponseDto<List<EmotionResponseDto>>> getAllEmotions() {
        return ResponseEntity.ok(ResponseDto.success(200, "감정 목록 조회 완료", emotionService.getAllEmotions()));
    }

    @GetMapping("/{emotionSeq}")
    public ResponseEntity<ResponseDto<EmotionResponseDto>> getEmotion(@PathVariable Integer emotionSeq) {
        return ResponseEntity.ok(ResponseDto.success(200, "감정 조회 완료", emotionService.getEmotion(emotionSeq)));
    }

    @GetMapping("/name/{name}")
    public ResponseEntity<ResponseDto<EmotionResponseDto>> getEmotionByName(@PathVariable String name) {
        return ResponseEntity.ok(ResponseDto.success(200, "감정 조회 완료", emotionService.getEmotionByName(name)));
    }

    @GetMapping("/statistics/two-weeks")
    public ResponseEntity<ResponseDto<EmotionStatisticsResponseDto>> getEmotionStatisticsForTwoWeeks(
            @RequestHeader("X-User-Seq") @NotNull Integer userSeq
    ) {
        EmotionStatisticsResponseDto response = emotionService.getEmotionStatistics(userSeq, 14);
        return ResponseEntity.ok(ResponseDto.success(200, "최근 2주 통계 조회 완료", response));
    }
    @GetMapping("/statistics/month")
    public ResponseEntity<ResponseDto<EmotionStatisticsResponseDto>> getEmotionStatisticsForMonth(
            @RequestHeader("X-User-Seq") @NotNull Integer userSeq
    ) {
        EmotionStatisticsResponseDto response = emotionService.getEmotionStatistics(userSeq, 30);
        return ResponseEntity.ok(ResponseDto.success(200, "최근 한 달 통계 조회 완료", response));
    }
}
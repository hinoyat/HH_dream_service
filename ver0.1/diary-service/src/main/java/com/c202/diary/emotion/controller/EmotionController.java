package com.c202.diary.emotion.controller;

import com.c202.diary.emotion.model.response.EmotionResponseDto;
import com.c202.diary.emotion.service.EmotionService;
import com.c202.dto.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/emotions")
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
}
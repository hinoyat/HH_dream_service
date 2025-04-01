package com.c202.diary.elastic.controller;

import com.c202.diary.elastic.model.request.DiarySearchRequestDto;
import com.c202.diary.elastic.model.response.DiarySearchListResponseDto;
import com.c202.diary.elastic.service.DiarySearchService;
import com.c202.dto.ResponseDto;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class DiarySearchController {

    private final DiarySearchService diarySearchService;

    @GetMapping("/diaries/search")
    public ResponseEntity<ResponseDto<List<DiarySearchListResponseDto>>> searchDiaries(
            @RequestHeader("X-User-Seq") @NotNull Integer userSeq,
            @ModelAttribute DiarySearchRequestDto requestDto) {

        List<DiarySearchListResponseDto> results = diarySearchService.searchDiaries(requestDto, userSeq);
        return ResponseEntity.ok(ResponseDto.success(200, "일기 검색 완료", results));
    }
}
package com.c202.diary.diary.controller;

import com.c202.diary.diary.model.request.DiaryCreateRequestDto;
import com.c202.diary.diary.model.request.DiaryUpdateRequestDto;
import com.c202.diary.diary.model.response.DiaryDetailResponseDto;
import com.c202.diary.diary.model.response.DiaryListResponseDto;
import com.c202.diary.diary.service.DiaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/diaries")
@RequiredArgsConstructor
public class DiaryController {

    private final DiaryService diaryService;

    @PostMapping("")
    public DiaryDetailResponseDto createDiary(@RequestHeader("X-User-Seq") Long userSeq, @RequestBody DiaryCreateRequestDto dto) {
        return diaryService.createDiary(userSeq, dto);
    }

    @PutMapping("/{diarySeq}")
    public DiaryDetailResponseDto updateDiary(@RequestBody DiaryUpdateRequestDto dto, @PathVariable Integer diarySeq) {
        return ResponseEntity.created()
        return diaryService.updateDiary(diarySeq, dto);
    }

    @DeleteMapping("/{diarySeq}")
    public int deleteDiary(@PathVariable Integer diarySeq) {
        diaryService.deleteDiary(diarySeq);
        return diarySeq;
    }

    // 조회 부분은 일단 더미 숫자로 넣었습니다 연동 성공 후 테스트 하면서 작업할게요!
    @GetMapping("/me")
    public List<DiaryListResponseDto> getMyDiaries(@RequestHeader("X-User-Seq") Long userSeq) {
        return diaryService.getMyDiaries(userSeq);
    }

    @GetMapping("/users/{userSeq}")
    public List<DiaryListResponseDto> getUserDiaries(@RequestHeader("X-User-Seq") Long userSeq) {
        return diaryService.getUserDiaries(userSeq);
    }

    // 개별 조회
    @GetMapping("/{diarySeq}")
    public DiaryDetailResponseDto getDiary(@PathVariable Integer diarySeq) {
        return diaryService.getDiary(diarySeq);
    }

    // 공개 비공개 토글
    @PutMapping("/{diarySeq}/visibility")
    public String toggleDiaryIsPublic(@PathVariable Integer diarySeq) {
        return diaryService.toggleDiaryIsPublic(diarySeq);
    }
}

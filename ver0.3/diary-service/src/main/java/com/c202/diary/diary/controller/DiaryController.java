package com.c202.diary.diary.controller;

import com.c202.diary.diary.model.request.VideoRequestDto;
import com.c202.diary.diary.model.response.UniverseDataResponseDto;
import com.c202.diary.like.model.response.DiaryLikeResponseDto;
import com.c202.diary.like.service.DiaryLikeService;
import com.c202.dto.ResponseDto;
import com.c202.diary.diary.model.request.DiaryCreateRequestDto;
import com.c202.diary.diary.model.request.DiaryUpdateRequestDto;
import com.c202.diary.diary.model.response.DiaryDetailResponseDto;
import com.c202.diary.diary.model.response.DiaryListResponseDto;
import com.c202.diary.diary.service.DiaryService;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/api/diaries")
@RequiredArgsConstructor
@Validated
public class DiaryController {

    private final DiaryService diaryService;
    private final DiaryLikeService diaryLikeService;

    @PostMapping("")
    public ResponseEntity<ResponseDto<DiaryDetailResponseDto>> createDiary(
            @RequestHeader("X-User-Seq") @NotNull Integer userSeq,
            @RequestBody DiaryCreateRequestDto dto
    ) {
        return ResponseEntity.status(201).body(ResponseDto.success(201, "일기 작성 완료", diaryService.createDiary(userSeq, dto)));
    }

    @PutMapping("/{diarySeq}")
    public ResponseEntity<ResponseDto<DiaryDetailResponseDto>> updateDiary(
            @RequestHeader("X-User-Seq") @NotNull Integer userSeq,
            @RequestBody DiaryUpdateRequestDto dto,
            @PathVariable Integer diarySeq
    ) {
        return ResponseEntity.ok(ResponseDto.success(200, "일기 수정 완료", diaryService.updateDiary(diarySeq, userSeq, dto)));
    }

    @DeleteMapping("/{diarySeq}")
    public ResponseEntity<ResponseDto<Objects>> deleteDiary(
            @RequestHeader("X-User-Seq") @NotNull Integer userSeq,
            @PathVariable Integer diarySeq
    ) {
        diaryService.deleteDiary(diarySeq, userSeq);
        return ResponseEntity.ok(ResponseDto.success(200, "일기 삭제 완료", null));
    }

    @GetMapping("/me")
    public ResponseEntity<ResponseDto<List<DiaryListResponseDto>>> getMyDiaries(
            @RequestHeader("X-User-Seq") @NotNull Integer userSeq
    ) {
        return ResponseEntity.ok(ResponseDto.success(200, "내 일기 조회 완료", diaryService.getMyDiaries(userSeq)));
    }

    @GetMapping("/users/{userSeq}")
    public ResponseEntity<ResponseDto<List<DiaryListResponseDto>>> getUserDiaries(
            @PathVariable Integer userSeq
    ) {
        return ResponseEntity.ok(ResponseDto.success(200, "사용자 일기 조회 완료", diaryService.getUserDiaries(userSeq)));
    }

    @GetMapping("/{diarySeq}")
    public ResponseEntity<ResponseDto<DiaryDetailResponseDto>> getDiary(
            @RequestHeader("X-User-Seq") @NotNull Integer userSeq,
            @PathVariable Integer diarySeq
    ) {
        return ResponseEntity.ok(ResponseDto.success(200, "일기 상세 조회 완료", diaryService.getDiary(diarySeq, userSeq)));
    }

    @PatchMapping("/{diarySeq}/visibility")
    public ResponseEntity<ResponseDto<DiaryDetailResponseDto>> toggleDiaryIsPublic(
            @RequestHeader("X-User-Seq") @NotNull Integer userSeq,
            @PathVariable Integer diarySeq
    ) {
        return ResponseEntity.ok(ResponseDto.success(200, "일기 공개 상태 변경 완료", diaryService.toggleDiaryIsPublic(diarySeq, userSeq)));
    }

    @GetMapping("/universe")
    public ResponseEntity<ResponseDto<UniverseDataResponseDto>> getUniverseData(
            @RequestHeader("X-User-Seq") @NotNull Integer userSeq
    ) {
        return ResponseEntity.ok(ResponseDto.success(200, "우주 데이터 조회 완료", diaryService.getUniverseData(userSeq)));
    }

    @PostMapping("/{diarySeq}/video")
    public ResponseEntity<ResponseDto<String>> uploadVideo(
            @RequestHeader("X-User-Seq") @NotNull Integer userSeq,
            @PathVariable Integer diarySeq,
            @RequestBody VideoRequestDto dto
            ) {
        diaryService.uploadVideo(diarySeq, userSeq, dto.getVideoUrl());
        return ResponseEntity.ok(ResponseDto.success(200, "동영상 업로드 완료"));
    }

    @PatchMapping("/{diarySeq}/like")
    public ResponseEntity<ResponseDto<DiaryLikeResponseDto>> addLike(
            @RequestHeader("X-User-Seq") @NotNull Integer userSeq,
            @PathVariable Integer diarySeq
    ) {
        String resultMessage = diaryLikeService.toggleLike(diarySeq, userSeq);
        return ResponseEntity.ok(ResponseDto.success(200, resultMessage, null));
    }

}

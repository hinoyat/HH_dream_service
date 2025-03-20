package com.c202.diary.diary.model.response;

import com.c202.diary.diary.entity.Diary;
import lombok.Builder;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@Builder
public class DiaryListResponseDto {

    private String title;

    private String content;

    private String videoUrl;

    private String dreamDate;

    private String createdAt;

    private String updatedAt;

    private String isPublic;

    public static List<DiaryListResponseDto> toDto(List<Diary> diaryList) {
        return diaryList.stream()
                .map(diary -> DiaryListResponseDto.builder()
                        .title(diary.getTitle())
                        .content(diary.getContent())
                        .videoUrl(diary.getVideoUrl())
                        .dreamDate(diary.getDreamDate())
                        .createdAt(diary.getCreatedAt())
                        .updatedAt(diary.getUpdatedAt())
                        .isPublic(diary.getIsPublic())
                        .build())
                .collect(Collectors.toList());

    }
}

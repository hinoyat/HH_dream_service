package com.c202.diary.diary.model.response;

import com.c202.diary.diary.entity.Diary;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DiaryDetailResponseDto {

    private String title;

    private String content;

    private String videoUrl;

    private String dreamDate;

    private String createdAt;

    private String updatedAt;

    private String isPublic;

    public static DiaryDetailResponseDto toDto(Diary diary) {
        return DiaryDetailResponseDto.builder()
                .title(diary.getTitle())
                .content(diary.getContent())
                .videoUrl(diary.getVideoUrl())
                .dreamDate(diary.getDreamDate())
                .createdAt(diary.getCreatedAt())
                .updatedAt(diary.getUpdatedAt())
                .isPublic(diary.getIsPublic())
                .build();
    }
}

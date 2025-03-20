package com.c202.diary.diary.model.response;

import com.c202.diary.diary.entity.Diary;
import com.c202.diary.tag.model.response.TagResponseDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class DiaryDetailResponseDto {

    private String title;

    private String content;

    private String videoUrl;

    private String dreamDate;

    private String createdAt;

    private String updatedAt;

    private String isPublic;

    private List<TagResponseDto> tags;


    public static DiaryDetailResponseDto toDto(Diary diary) {
        return toDto(diary, null);
    }

    public static DiaryDetailResponseDto toDto(Diary diary, List<TagResponseDto> tags) {
        return DiaryDetailResponseDto.builder()
                .title(diary.getTitle())
                .content(diary.getContent())
                .videoUrl(diary.getVideoUrl())
                .dreamDate(diary.getDreamDate())
                .createdAt(diary.getCreatedAt())
                .updatedAt(diary.getUpdatedAt())
                .isPublic(diary.getIsPublic())
                .tags(tags)
                .build();
    }
}

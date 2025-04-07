package com.c202.diary.diary.model.response;

import com.c202.diary.diary.entity.Diary;
import com.c202.diary.tag.model.response.TagResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.stream.Collectors;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiaryListResponseDto {

    private Integer diarySeq;
    private String title;
    private String content;
    private String dreamDate;
    private String createdAt;
    private String isPublic;
    private Double x;
    private Double y;
    private Double z;
    private Integer emotionSeq;
    private String emotionName;
    private List<TagResponseDto> tags;

    public static List<DiaryListResponseDto> toDto(List<Diary> diaries) {
        return diaries.stream()
                .map(DiaryListResponseDto::toDto)
                .collect(Collectors.toList());
    }

    public static DiaryListResponseDto toDto(Diary diary) {
        return DiaryListResponseDto.builder()
                .diarySeq(diary.getDiarySeq())
                .title(diary.getTitle())
                .content(diary.getContent())
                .dreamDate(diary.getDreamDate())
                .createdAt(diary.getCreatedAt())
                .isPublic(diary.getIsPublic())
                .x(diary.getX())
                .y(diary.getY())
                .z(diary.getZ())
                .emotionSeq(diary.getEmotionSeq())
                .build();
    }

    // 감정 이름을 포함한 DTO
    public static DiaryListResponseDto toDto(Diary diary, String emotionName) {
        return DiaryListResponseDto.builder()
                .diarySeq(diary.getDiarySeq())
                .title(diary.getTitle())
                .content(diary.getContent())
                .dreamDate(diary.getDreamDate())
                .createdAt(diary.getCreatedAt())
                .isPublic(diary.getIsPublic())
                .x(diary.getX())
                .y(diary.getY())
                .z(diary.getZ())
                .emotionSeq(diary.getEmotionSeq())
                .emotionName(emotionName)
                .build();
    }

    // 태그와 감정 이름을 포함한 DTO
    public static DiaryListResponseDto toDto(Diary diary, String emotionName, List<TagResponseDto> tags) {
        return DiaryListResponseDto.builder()
                .diarySeq(diary.getDiarySeq())
                .title(diary.getTitle())
                .content(diary.getContent())
                .dreamDate(diary.getDreamDate())
                .createdAt(diary.getCreatedAt())
                .isPublic(diary.getIsPublic())
                .x(diary.getX())
                .y(diary.getY())
                .z(diary.getZ())
                .emotionSeq(diary.getEmotionSeq())
                .emotionName(emotionName)
                .tags(tags)
                .build();
    }

    // 감정 이름 리스트를 포함한 목록 DTO
    public static List<DiaryListResponseDto> toDto(List<Diary> diaries, List<String> emotionNames) {
        // 일기 목록과 감정 이름 목록의 크기가 같아야 함
        if (diaries.size() != emotionNames.size()) {
            throw new IllegalArgumentException("Diaries list and emotion names list must be the same size");
        }

        List<DiaryListResponseDto> dtoList = new java.util.ArrayList<>();
        for (int i = 0; i < diaries.size(); i++) {
            dtoList.add(toDto(diaries.get(i), emotionNames.get(i)));
        }

        return dtoList;
    }
}
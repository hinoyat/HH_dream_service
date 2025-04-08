package com.c202.diary.diary.model.response;

import com.c202.diary.diary.entity.Diary;
import com.c202.diary.tag.model.response.TagResponseDto;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class DiaryDetailResponseDto {

    private Integer diarySeq;

    private Integer userSeq;

    private String title;

    private String content;

    private String videoUrl;

    private String dreamDate;

    private String createdAt;

    private String updatedAt;

    private String isPublic;

    private List<TagResponseDto> tags;

    private Integer likeCount;

    private boolean hasLiked;

    private Double x;
    private Double y;
    private Double z;
    private Integer emotionSeq;
    private String emotionName;
    private List<Integer> connectedDiaries;

    public static DiaryDetailResponseDto toDto(Diary diary, List<TagResponseDto> tags) {
        return toDto(diary, tags, null, null, 0, false);
    }

    public static DiaryDetailResponseDto toDto(Diary diary, List<TagResponseDto> tags, String emotionName) {
        return toDto(diary, tags, emotionName, null, 0, false);
    }

    public static DiaryDetailResponseDto toDto(Diary diary, List<TagResponseDto> tags,
                                               String emotionName, List<Integer> connectedDiaries) {
        return toDto(diary, tags, emotionName, connectedDiaries, 0, false);
    }

    public static DiaryDetailResponseDto toDto(Diary diary, List<TagResponseDto> tags,
                                               String emotionName, List<Integer> connectedDiaries,
                                               Integer likeCount, boolean hasLiked) {
        return DiaryDetailResponseDto.builder()
                .diarySeq(diary.getDiarySeq())
                .userSeq(diary.getUserSeq())
                .title(diary.getTitle())
                .content(diary.getContent())
                .videoUrl(diary.getVideoUrl())
                .dreamDate(diary.getDreamDate())
                .createdAt(diary.getCreatedAt())
                .updatedAt(diary.getUpdatedAt())
                .isPublic(diary.getIsPublic())
                .x(diary.getX())
                .y(diary.getY())
                .z(diary.getZ())
                .emotionSeq(diary.getEmotionSeq())
                .emotionName(emotionName)
                .tags(tags)
                .connectedDiaries(connectedDiaries)
                .likeCount(likeCount)
                .hasLiked(hasLiked)
                .build();
    }
}

package com.c202.diary.like.model.response;

import com.c202.diary.like.entity.DiaryLike;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiaryLikeResponseDto {
    private Integer likeSeq;
    private Integer diarySeq;
    private Integer userSeq;
    private String createdAt;
    private Integer likeCount; // 해당 일기의 총 좋아요 수

    public static DiaryLikeResponseDto toDto(DiaryLike diaryLike, Integer likeCount) {
        return DiaryLikeResponseDto.builder()
                .likeSeq(diaryLike.getLikeSeq())
                .diarySeq(diaryLike.getDiarySeq())
                .userSeq(diaryLike.getUserSeq())
                .createdAt(diaryLike.getCreatedAt())
                .likeCount(likeCount)
                .build();
    }
}

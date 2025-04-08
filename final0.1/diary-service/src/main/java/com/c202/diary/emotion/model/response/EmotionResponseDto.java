package com.c202.diary.emotion.model.response;

import com.c202.diary.emotion.entity.Emotion;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmotionResponseDto {

    private Integer emotionSeq;
    private String name;
    private Double baseX;
    private Double baseY;
    private Double baseZ;
    private Double baseRadius;
    private Integer diaryCount;

    public static EmotionResponseDto toDto(Emotion emotion) {
        return EmotionResponseDto.builder()
                .emotionSeq(emotion.getEmotionSeq())
                .name(emotion.getName())
                .baseX(emotion.getBaseX())
                .baseY(emotion.getBaseY())
                .baseZ(emotion.getBaseZ())
                .baseRadius(emotion.getBaseRadius())
                .diaryCount(emotion.getDiaryCount())
                .build();
    }
}
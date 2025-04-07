package com.c202.diary.emotion.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmotionStatisticsResponseDto {

    private Integer periodDays;
    private List<EmotionCount> data;

    @Getter
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class EmotionCount {
        private String emotion;
        private Long count;
    }
}

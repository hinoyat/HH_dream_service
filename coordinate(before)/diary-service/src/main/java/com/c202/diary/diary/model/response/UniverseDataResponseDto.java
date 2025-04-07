package com.c202.diary.diary.model.response;

import com.c202.diary.emotion.model.response.EmotionResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UniverseDataResponseDto {

    private List<DiaryListResponseDto> diaries;

    private List<EmotionResponseDto> emotions;

    private Map<Integer, List<Integer>> connections;
}
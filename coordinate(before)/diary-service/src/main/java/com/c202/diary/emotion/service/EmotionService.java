package com.c202.diary.emotion.service;

import com.c202.diary.emotion.model.response.EmotionResponseDto;
import com.c202.diary.emotion.model.response.EmotionStatisticsResponseDto;

import java.util.List;

public interface EmotionService {

    List<EmotionResponseDto> getAllEmotions();

    EmotionResponseDto getEmotion(Integer emotionSeq);


    EmotionResponseDto getEmotionByName(String name);

    void incrementDiaryCount(Integer emotionSeq);

    void decrementDiaryCount(Integer emotionSeq);

    EmotionStatisticsResponseDto getEmotionStatistics(Integer userSeq, Integer periodDays);
}
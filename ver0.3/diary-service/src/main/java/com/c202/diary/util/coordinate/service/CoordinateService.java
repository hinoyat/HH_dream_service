package com.c202.diary.util.coordinate.service;

import com.c202.diary.util.coordinate.model.CoordinateDto;
import com.c202.diary.diary.entity.Diary;

import java.util.List;

public interface CoordinateService {

    CoordinateDto generateCoordinates(String mainEmotion, List<String> tags, Integer diarySeq);

    CoordinateDto updateCoordinates(Diary diary, String mainEmotion, List<String> tags);

    List<Integer> findSimilarDiaries(Integer diarySeq, int maxResults);
}
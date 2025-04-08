package com.c202.diary.util.coordinate.service;

import com.c202.diary.util.coordinate.model.CoordinateDto;
import com.c202.diary.diary.entity.Diary;

import java.util.List;
import java.util.Map;

/**
 * 일기의 3D 좌표 생성 및 관리를 위한 인터페이스
 */
public interface CoordinateService {


    CoordinateDto generateCoordinates(String mainEmotion, List<String> tags, Integer diarySeq);

    CoordinateDto updateCoordinates(Diary diary, String mainEmotion, List<String> tags);

    List<Integer> findSimilarDiaries(Integer diarySeq, int maxResults);

    Map<Integer, List<Integer>> relayoutUniverse(Integer userSeq);
}
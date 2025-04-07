package com.c202.diary.diary.service;

import com.c202.diary.diary.model.request.DiaryCreateRequestDto;
import com.c202.diary.diary.model.request.DiaryUpdateRequestDto;
import com.c202.diary.diary.model.response.DiaryDetailResponseDto;
import com.c202.diary.diary.model.response.DiaryListResponseDto;
import com.c202.diary.diary.model.response.UniverseDataResponseDto;

import java.util.List;

public interface DiaryService {

    DiaryDetailResponseDto createDiary(Integer userSeq, DiaryCreateRequestDto request);

    DiaryDetailResponseDto updateDiary(Integer diarySeq, Integer userSeq, DiaryUpdateRequestDto request);

    void deleteDiary(Integer diaryId, Integer userSeq);

    List<DiaryListResponseDto> getMyDiaries(Integer userSeq);

    List<DiaryListResponseDto> getUserDiaries(Integer userSeq);

    DiaryDetailResponseDto getDiary(Integer diarySeq, Integer userSeq);

    DiaryDetailResponseDto toggleDiaryIsPublic(Integer diarySeq, Integer userSeq);

    UniverseDataResponseDto getUniverseData(Integer userSeq);

    void uploadVideo(Integer diarySeq, Integer userSeq, String videoUrl);

    void relayoutAllDiaries(Integer userSeq);
}
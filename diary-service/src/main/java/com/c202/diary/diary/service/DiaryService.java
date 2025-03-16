package com.c202.diary.diary.service;

import com.c202.diary.diary.model.request.DiaryCreateRequestDto;
import com.c202.diary.diary.model.request.DiaryUpdateRequestDto;
import com.c202.diary.diary.model.response.DiaryDetailResponseDto;
import com.c202.diary.diary.model.response.DiaryListResponseDto;

import java.util.List;

public interface DiaryService {

    DiaryDetailResponseDto createDiary(Long userSeq, DiaryCreateRequestDto request);

    DiaryDetailResponseDto updateDiary(int diarySeq, DiaryUpdateRequestDto request);

    void deleteDiary(int diaryId);

    List<DiaryListResponseDto> getMyDiaries(Long userSeq);

    List<DiaryListResponseDto> getUserDiaries(Long userSeq);

    DiaryDetailResponseDto getDiary(int diarySeq);

    String toggleDiaryIsPublic(int diarySeq);

//    boolean toggleDiaryLike(String userId, Long diaryId);
}
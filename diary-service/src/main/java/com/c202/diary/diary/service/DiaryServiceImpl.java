package com.c202.diary.diary.service;

import com.c202.diary.diary.entity.Diary;
import com.c202.diary.diary.model.request.DiaryCreateRequestDto;
import com.c202.diary.diary.model.request.DiaryUpdateRequestDto;
import com.c202.diary.diary.model.response.DiaryDetailResponseDto;
import com.c202.diary.diary.model.response.DiaryListResponseDto;
import com.c202.diary.diary.repository.DiaryRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DiaryServiceImpl implements DiaryService {

    private final DiaryRepository diaryRepository;

    // 날짜 포매터
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd HHmmss");

    @Transactional
    @Override
    public DiaryDetailResponseDto createDiary(Long userSeq, DiaryCreateRequestDto request) {

        String now = LocalDateTime.now().format(DATE_TIME_FORMATTER);

        Diary diary = Diary.builder()
                .userSeq(userSeq)
                .title(request.getTitle())
                .content(request.getContent())
                .dreamDate(request.getDreamDate())
                .isPublic(request.getIsPublic())
                .createdAt(now)
                .updatedAt(now)
                .isDeleted("N")
                .build();

        diaryRepository.save(diary);

        return DiaryDetailResponseDto.toDto(diary);
    }

    @Transactional
    @Override
    public DiaryDetailResponseDto updateDiary(int diarySeq, DiaryUpdateRequestDto request) {
        // 다이어리 조회
        Diary diary = diaryRepository.findByDiarySeq(diarySeq);

        String now = LocalDateTime.now().format(DATE_TIME_FORMATTER);
        // 업데이트
        diary.setTitle(request.getTitle());
        diary.setContent(request.getContent());
        diary.setDreamDate(request.getDreamDate());
        diary.setUpdatedAt(now);

        // 저장
        diaryRepository.save(diary);

        return DiaryDetailResponseDto.toDto(diary);
    }

    @Transactional
    @Override
    public void deleteDiary(int diarySeq) {

        Diary diary = diaryRepository.findByDiarySeq(diarySeq);

        diary.deleteDiary();
    }

    // 전체 조회(유저 연동 되면 분기해서 조회)
    @Transactional
    @Override
    public List<DiaryListResponseDto> getMyDiaries(Long userSeq) {
        List<Diary> diaries = diaryRepository.findByUserSeq(userSeq);

        return DiaryListResponseDto.toDto(diaries);
    }

    @Transactional
    @Override
    // 전체 조회(유저 연동 되면 분기해서 조회)
    public List<DiaryListResponseDto> getUserDiaries(Long userSeq) {
        List<Diary> diaries = diaryRepository.findByUserSeq(userSeq);
        return DiaryListResponseDto.toDto(diaries);
    }
    
    // 개별 상세 조회
    @Transactional
    @Override
    public DiaryDetailResponseDto getDiary(int diarySeq) {
        Diary diary = diaryRepository.findByDiarySeq(diarySeq);
        return DiaryDetailResponseDto.toDto(diary);
    }
    
    @Transactional
    @Override
    public String toggleDiaryIsPublic(int diarySeq) {
        Diary diary = diaryRepository.findByDiarySeq(diarySeq);

        String result;

        String isPublic = diary.getIsPublic();

        if (isPublic.equals("Y")) {
            diary.setIsPublic("N");
            result = "비공개 설정 완료";
        } else {
            diary.setIsPublic("Y");
            result = "공개 설정 완료";
        }
        diaryRepository.save(diary);

        return result;
    }

}

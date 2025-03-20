package com.c202.diary.diary.service;

import com.c202.diary.diary.entity.Diary;
import com.c202.diary.diary.model.request.DiaryCreateRequestDto;
import com.c202.diary.diary.model.request.DiaryUpdateRequestDto;
import com.c202.diary.diary.model.response.DiaryDetailResponseDto;
import com.c202.diary.diary.model.response.DiaryListResponseDto;
import com.c202.diary.diary.repository.DiaryRepository;
import com.c202.diary.tag.entity.DiaryTag;
import com.c202.diary.tag.model.response.TagResponseDto;
import com.c202.diary.tag.repository.DiaryTagRepository;
import com.c202.diary.tag.service.TagService;
import com.c202.exception.CustomException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiaryServiceImpl implements DiaryService {

    private final TagService tagService;
    private final DiaryRepository diaryRepository;
    private final DiaryTagRepository diaryTagRepository;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd HHmmss");

    @Transactional
    @Override
    public DiaryDetailResponseDto createDiary(Integer userSeq, DiaryCreateRequestDto request) {

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

        List<TagResponseDto> tagDtos = new ArrayList<>();
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            tagDtos = tagService.processTags(diary, request.getTags(), now);
        }

        return DiaryDetailResponseDto.toDto(diary, tagDtos);
    }

    @Transactional
    @Override
    public DiaryDetailResponseDto updateDiary(Integer diarySeq, Integer userSeq, DiaryUpdateRequestDto request) {
        Diary diary = validateDiary(diarySeq, userSeq);
        String now = LocalDateTime.now().format(DATE_TIME_FORMATTER);

        diary.update(
                request.getTitle(),
                request.getContent(),
                request.getDreamDate(),
                now
        );

        List<TagResponseDto> tagDtos = new ArrayList<>();

        diaryTagRepository.deleteByDiary(diary);

        tagDtos = tagService.processTags(diary, request.getTags(), now);

        diaryRepository.save(diary);

        return DiaryDetailResponseDto.toDto(diary, tagDtos);
    }

    @Transactional
    @Override
    public void deleteDiary(Integer diarySeq, Integer userSeq) {
        Diary diary = validateDiary(diarySeq, userSeq);
        diary.deleteDiary();
    }

    @Transactional
    @Override
    public List<DiaryListResponseDto> getMyDiaries(Integer userSeq) {
        List<Diary> diaries = diaryRepository.findByUserSeqAndIsDeleted(userSeq, "N");
        return DiaryListResponseDto.toDto(diaries);
    }

    @Transactional
    @Override
    public List<DiaryListResponseDto> getUserDiaries(Integer userSeq) {
        List<Diary> diaries = diaryRepository.findByUserSeqAndIsPublicAndIsDeleted(userSeq, "Y", "N");
        return DiaryListResponseDto.toDto(diaries);
    }

    @Transactional
    @Override
    public DiaryDetailResponseDto getDiary(Integer diarySeq) {
        Diary diary = diaryRepository.findByDiarySeqAndIsDeleted(diarySeq, "N")
                .orElseThrow(() -> new CustomException("해당 일기를 찾을 수 없습니다."));

        List<TagResponseDto> tagDtos = getTagsForDiary(diary);

        return DiaryDetailResponseDto.toDto(diary, tagDtos);
    }
    
    @Transactional
    @Override
    public DiaryDetailResponseDto toggleDiaryIsPublic(Integer diarySeq, Integer userSeq) {

        Diary diary = validateDiary(diarySeq, userSeq);

        String isPublic = diary.getIsPublic();

        if (isPublic.equals("Y")) {
            diary.setPublic("N");
        } else {
            diary.setPublic("Y");
        }
        diaryRepository.save(diary);

        List<TagResponseDto> tagDtos = getTagsForDiary(diary);

        return DiaryDetailResponseDto.toDto(diary, tagDtos);

    }

    // 일기 유효성 검증
    private Diary validateDiary(Integer diarySeq, Integer userSeq) {
        Diary diary = diaryRepository.findByDiarySeqAndIsDeleted(diarySeq, "N")
                .orElseThrow(() -> new CustomException("해당 일기를 찾을 수 없습니다."));
        if (!diary.getUserSeq().equals(userSeq)) {
            throw new CustomException("해당 일기에 대한 권한이 없습니다.");
        }
        return diary;
    }

    // 태그 조회에 사용
    private List<TagResponseDto> getTagsForDiary(Diary diary) {
        List<DiaryTag> diaryTags = diaryTagRepository.findByDiary(diary);
        return diaryTags.stream()
                .map(diaryTag -> TagResponseDto.toDto(diaryTag.getTag()))
                .collect(Collectors.toList());
    }

}

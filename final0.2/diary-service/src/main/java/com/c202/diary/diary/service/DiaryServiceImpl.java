package com.c202.diary.diary.service;

import com.c202.diary.elastic.service.DiaryIndexService;
import com.c202.diary.util.coordinate.model.CoordinateDto;
import com.c202.diary.util.coordinate.service.CoordinateService;
import com.c202.diary.diary.entity.Diary;
import com.c202.diary.diary.model.request.DiaryCreateRequestDto;
import com.c202.diary.diary.model.request.DiaryUpdateRequestDto;
import com.c202.diary.diary.model.response.DiaryDetailResponseDto;
import com.c202.diary.diary.model.response.DiaryListResponseDto;
import com.c202.diary.diary.model.response.UniverseDataResponseDto;
import com.c202.diary.diary.repository.DiaryRepository;
import com.c202.diary.emotion.entity.Emotion;
import com.c202.diary.emotion.model.response.EmotionResponseDto;
import com.c202.diary.emotion.repository.EmotionRepository;
import com.c202.diary.emotion.service.EmotionService;
import com.c202.diary.like.service.DiaryLikeService;
import com.c202.diary.util.rabbitmq.AlarmService;
import com.c202.diary.util.s3.S3Service;
import com.c202.diary.tag.entity.DiaryTag;
import com.c202.diary.tag.model.response.TagResponseDto;
import com.c202.diary.tag.repository.DiaryTagRepository;
import com.c202.diary.tag.service.TagService;
import com.c202.exception.types.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DiaryServiceImpl implements DiaryService {

    private final TagService tagService;
    private final DiaryLikeService diaryLikeService;
    private final S3Service s3Service;
    private final DiaryRepository diaryRepository;
    private final DiaryTagRepository diaryTagRepository;
    private final EmotionRepository emotionRepository;
    private final EmotionService emotionService;
    private final CoordinateService coordinateService;
    private final AlarmService alarmService;
    private final DiaryIndexService diaryIndexService;
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd HHmmss");

    @Transactional
    @Override
    public DiaryDetailResponseDto createDiary(Integer userSeq, DiaryCreateRequestDto request) {

        String now = LocalDateTime.now().format(DATE_TIME_FORMATTER);

        // 감정 검증
        Emotion emotion = emotionRepository.findByName(request.getMainEmotion())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 감정입니다: " + request.getMainEmotion()));

        String emotionName = emotion.getName();

        // 좌표 생성
        CoordinateDto coordinates = coordinateService.generateCoordinates(
                request.getMainEmotion(), request.getTags(), null);

        Diary diary = Diary.builder()
                .userSeq(userSeq)
                .title(request.getTitle())
                .content(request.getContent())
                .dreamDate(request.getDreamDate())
                .isPublic(request.getIsPublic())
                .createdAt(now)
                .updatedAt(now)
                .isDeleted("N")
                .x(coordinates.getX())
                .y(coordinates.getY())
                .z(coordinates.getZ())
                .emotionSeq(coordinates.getEmotionSeq())
                .build();

        emotionService.incrementDiaryCount(emotion.getEmotionSeq());

        diaryRepository.save(diary);

        diaryIndexService.indexDiary(diary); // ElasticSearch 색인 업데이트

        alarmService.sendDiaryCreatedAlarm(
                diary.getUserSeq(),
                diary.getTitle(),
                diary.getDiarySeq()
        );

        List<TagResponseDto> tagDtos = new ArrayList<>();
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            tagDtos = tagService.processTags(diary, request.getTags(), now);
        }

        coordinateService.relayoutUniverse(userSeq);

        return DiaryDetailResponseDto.toDto(diary, tagDtos, emotionName);
    }

    @Transactional
    @Override
    public DiaryDetailResponseDto updateDiary(Integer diarySeq, Integer userSeq, DiaryUpdateRequestDto request) {
        Diary diary = validateDiary(diarySeq, userSeq);
        String now = LocalDateTime.now().format(DATE_TIME_FORMATTER);

        diary.update(
                request.getTitle(),
                request.getContent(),
                request.getIsPublic(),
                request.getDreamDate(),
                now
        );

        Integer oldEmotionSeq = diary.getEmotionSeq();
        Emotion newEmotion = emotionRepository.findByName(request.getMainEmotion())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 감정입니다: " + request.getMainEmotion()));

        boolean emotionChanged = oldEmotionSeq == null || !oldEmotionSeq.equals(newEmotion.getEmotionSeq());


        List<TagResponseDto> tagDtos = new ArrayList<>();

        diaryTagRepository.deleteByDiary(diary);

        tagDtos = tagService.processTags(diary, request.getTags(), now);

        // 좌표 업데이트
        CoordinateDto coordinates;
        if (emotionChanged) {
            // 이전 감정 카운트 감소
            if (oldEmotionSeq != null) {
                emotionService.decrementDiaryCount(oldEmotionSeq);
            }

            // 새 감정에 좌표 생성
            coordinates = coordinateService.generateCoordinates(
                    request.getMainEmotion(), request.getTags(), diary.getDiarySeq());

            // 새 감정 카운트 증가
            emotionService.incrementDiaryCount(newEmotion.getEmotionSeq());
        } else {
            // 같은 감정 내에서 위치 조정
            coordinates = coordinateService.updateCoordinates(diary, request.getMainEmotion(), request.getTags());
        }

        diary.setCoordinates(coordinates.getX(), coordinates.getY(), coordinates.getZ(), coordinates.getEmotionSeq());

        diaryRepository.save(diary);

        diaryIndexService.indexDiary(diary);

        List<Integer> connectedDiaries = coordinateService.findSimilarDiaries(diary.getDiarySeq(), 5);

        Integer likeCount = diaryLikeService.getLikeCount(diarySeq);
        boolean hasLiked = diaryLikeService.hasUserLiked(diarySeq, userSeq);


        coordinateService.relayoutUniverse(userSeq);

        return DiaryDetailResponseDto.toDto(diary, tagDtos, newEmotion.getName(), connectedDiaries, likeCount, hasLiked);
    }

    @Transactional
    @Override
    public void deleteDiary(Integer diarySeq, Integer userSeq) {
        Diary diary = validateDiary(diarySeq, userSeq);

        if (diary.getEmotionSeq() != null) {
            emotionService.decrementDiaryCount(diary.getEmotionSeq());
        }

        diaryIndexService.indexDiary(diary);

        diary.deleteDiary();

        coordinateService.relayoutUniverse(userSeq);
    }

    @Transactional
    @Override
    public List<DiaryListResponseDto> getMyDiaries(Integer userSeq) {
        List<Diary> diaries = diaryRepository.findByUserSeqAndIsDeleted(userSeq, "N");
        List<DiaryListResponseDto> result = new ArrayList<>();

        for (Diary diary : diaries) {
            String emotionName = "";
            if (diary.getEmotionSeq() != null) {
                emotionName = emotionRepository.findById(diary.getEmotionSeq())
                        .map(Emotion::getName)
                        .orElse("");
            }

            List<TagResponseDto> tags = getTagsForDiary(diary);

            result.add(DiaryListResponseDto.toDto(diary, emotionName, tags));
        }

        return result;
    }

    @Transactional
    @Override
    public List<DiaryListResponseDto> getUserDiaries(Integer userSeq) {
        List<Diary> diaries = diaryRepository.findByUserSeqAndIsPublicAndIsDeleted(userSeq, "Y", "N");
        List<DiaryListResponseDto> result = new ArrayList<>();

        for (Diary diary : diaries) {
            String emotionName = "";
            if (diary.getEmotionSeq() != null) {
                emotionName = emotionRepository.findById(diary.getEmotionSeq())
                        .map(Emotion::getName)
                        .orElse("");
            }

            List<TagResponseDto> tags = getTagsForDiary(diary);

            result.add(DiaryListResponseDto.toDto(diary, emotionName, tags));
        }

        return result;
    }

    @Transactional
    @Override
    public DiaryDetailResponseDto getDiary(Integer diarySeq, Integer userSeq) {
        Diary diary = diaryRepository.findByDiarySeqAndIsDeleted(diarySeq, "N")
                .orElseThrow(() -> new NotFoundException("해당 일기를 찾을 수 없습니다."));

        List<TagResponseDto> tagDtos = getTagsForDiary(diary);

        String emotionName = "";
        if (diary.getEmotionSeq() != null) {
            emotionName = emotionRepository.findById(diary.getEmotionSeq())
                    .map(Emotion::getName)
                    .orElse("");
        }

        // 연결된 일기 목록 찾기
        List<Integer> connectedDiaries = coordinateService.findSimilarDiaries(diary.getDiarySeq(), 5);
        Integer likeCount = diaryLikeService.getLikeCount(diarySeq);
        boolean hasLiked = diaryLikeService.hasUserLiked(diarySeq, userSeq);

        return DiaryDetailResponseDto.toDto(diary, tagDtos, emotionName, connectedDiaries, likeCount, hasLiked);
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

        diaryIndexService.indexDiary(diary);

        List<TagResponseDto> tagDtos = getTagsForDiary(diary);

        // 감정 이름 가져오기
        String emotionName = "";
        if (diary.getEmotionSeq() != null) {
            emotionName = emotionRepository.findById(diary.getEmotionSeq())
                    .map(Emotion::getName)
                    .orElse("");
        }

        List<Integer> connectedDiaries = coordinateService.findSimilarDiaries(diary.getDiarySeq(), 5);

        Integer likeCount = diaryLikeService.getLikeCount(diarySeq);
        boolean hasLiked = diaryLikeService.hasUserLiked(diarySeq, userSeq);


        return DiaryDetailResponseDto.toDto(diary, tagDtos, emotionName, connectedDiaries, likeCount, hasLiked);
    }

    @Transactional
    @Override
    public UniverseDataResponseDto getUniverseData(Integer userSeq) {
        // 사용자의 모든 일기 가져오기
        List<Diary> diaries = diaryRepository.findByUserSeqAndIsDeleted(userSeq, "N");

        // 일기별 감정 이름 매핑
        Map<Integer, String> emotionNames = new HashMap<>();
        for (Diary diary : diaries) {
            if (diary.getEmotionSeq() != null) {
                String emotionName = emotionRepository.findById(diary.getEmotionSeq())
                        .map(Emotion::getName)
                        .orElse("");
                emotionNames.put(diary.getDiarySeq(), emotionName);
            }
        }

        // 일기 DTO 생성
        List<DiaryListResponseDto> diaryDtos = diaries.stream()
                .map(diary -> DiaryListResponseDto.toDto(diary, emotionNames.get(diary.getDiarySeq())))
                .collect(Collectors.toList());

        // 모든 감정 영역 정보 가져오기
        List<EmotionResponseDto> emotions = emotionService.getAllEmotions();

        // 일기 연결 정보 생성
        Map<Integer, List<Integer>> connections = new HashMap<>();
        for (Diary diary : diaries) {
            List<Integer> connected = coordinateService.findSimilarDiaries(diary.getDiarySeq(), 5);
            connections.put(diary.getDiarySeq(), connected);
        }

        // 우주 데이터 DTO 반환
        return UniverseDataResponseDto.builder()
                .diaries(diaryDtos)
                .emotions(emotions)
                .connections(connections)
                .build();
    }

    @Transactional
    @Override
    public void uploadVideo(Integer diarySeq, Integer userSeq, String videoUrl) {
        Diary diary = validateDiary(diarySeq, userSeq);
        try {

            String newUrl = s3Service.uploadVideoFromUrl(videoUrl);

            diary.setVideo(newUrl);

            alarmService.sendVideoCreatedAlarm(
                    diary.getUserSeq(),
                    diary.getTitle(),
                    diary.getDiarySeq()
            );
            diaryRepository.save(diary);
        } catch (Exception e) {
            alarmService.sendVideoFailedAlarm(
                    diary.getUserSeq(),
                    diary.getTitle(),
                    diary.getDiarySeq()
            );
            throw new AiCallFailedException("영상 생성에 실패했습니다");
        }
    }

    @Transactional
    @Override
    public void relayoutAllDiaries(Integer userSeq) {
        // 새로운 CoordinateService의 relayoutUniverse 메서드 호출
        Map<Integer, List<Integer>> connections = coordinateService.relayoutUniverse(userSeq);
        // 로그 출력 등 필요 시 추가
    }




    // 일기 유효성 검증
    private Diary validateDiary(Integer diarySeq, Integer userSeq) {
        Diary diary = diaryRepository.findByDiarySeqAndIsDeleted(diarySeq, "N")
                .orElseThrow(() -> new NotFoundException("해당 일기를 찾을 수 없습니다."));
        if (!diary.getUserSeq().equals(userSeq)) {
            throw new UnauthorizedException("해당 일기에 대한 권한이 없습니다.");
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

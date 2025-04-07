package com.c202.diary.util.coordinate.service;

import com.c202.diary.util.coordinate.model.CoordinateDto;
import com.c202.diary.diary.entity.Diary;
import com.c202.diary.diary.repository.DiaryRepository;
import com.c202.diary.emotion.entity.Emotion;
import com.c202.diary.emotion.repository.EmotionRepository;
import com.c202.exception.types.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@RequiredArgsConstructor
public class CoordinateServiceImpl implements CoordinateService {

    private final EmotionRepository emotionRepository;
    private final DiaryRepository diaryRepository;
    private final ConstellationLayoutService layoutService;
    private final CoordinateResetService coordinateResetService;
    private final ConstellationConnectionService connectionService;

    @Override
    public CoordinateDto generateCoordinates(String mainEmotion, List<String> tags, Integer diarySeq) {
        // 1. 감정 정보 조회
        Emotion emotion = emotionRepository.findByName(mainEmotion)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 감정입니다: " + mainEmotion));

        // 2. 임시 일기 객체 생성 (좌표 생성용)
        Diary tempDiary = Diary.builder()
                .diarySeq(diarySeq != null ? diarySeq : -1) // 임시 ID 사용
                .emotionSeq(emotion.getEmotionSeq())
                .build();

        // 3. 별자리 패턴에 맞는 최적의 좌표 생성
        CoordinateDto coordinates = coordinateResetService.generateOptimalCoordinates(tempDiary);

        log.info("새 일기 좌표 생성 완료: emotion={}, x={}, y={}, z={}",
                mainEmotion, coordinates.getX(), coordinates.getY(), coordinates.getZ());

        return coordinates;
    }

    @Override
    public CoordinateDto updateCoordinates(Diary diary, String mainEmotion, List<String> tags) {
        // 1. 감정 변경 여부 확인
        Emotion currentEmotion = diary.getEmotionSeq() != null ?
                emotionRepository.findById(diary.getEmotionSeq())
                        .orElse(null) : null;

        Emotion targetEmotion = emotionRepository.findByName(mainEmotion)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 감정입니다: " + mainEmotion));

        // 2. 감정이 변경된 경우 새 좌표 생성
        if (currentEmotion == null || !currentEmotion.getEmotionSeq().equals(targetEmotion.getEmotionSeq())) {
            log.info("감정이 변경되어 새 좌표 생성: diary={}, oldEmotion={}, newEmotion={}",
                    diary.getDiarySeq(),
                    currentEmotion != null ? currentEmotion.getName() : "없음",
                    mainEmotion);

            return generateCoordinates(mainEmotion, tags, diary.getDiarySeq());
        }

        // 3. 동일 감정 내 위치 조정 (별자리 패턴 유지)
        // 임시 복제 객체 생성
        Diary tempDiary = Diary.builder()
                .diarySeq(diary.getDiarySeq())
                .emotionSeq(targetEmotion.getEmotionSeq())
                .x(diary.getX())
                .y(diary.getY())
                .z(diary.getZ())
                .build();

        // 최적 위치 계산
        CoordinateDto optimizedCoordinates = coordinateResetService.generateOptimalCoordinates(tempDiary);

        log.info("일기 좌표 업데이트 완료: diary={}, emotion={}, x={}, y={}, z={}",
                diary.getDiarySeq(), mainEmotion,
                optimizedCoordinates.getX(),
                optimizedCoordinates.getY(),
                optimizedCoordinates.getZ());

        return optimizedCoordinates;
    }

    @Override
    public List<Integer> findSimilarDiaries(Integer diarySeq, int maxResults) {
        // 1. 일기 조회
        Diary diary = diaryRepository.findByDiarySeq(diarySeq)
                .orElseThrow(() -> new NotFoundException("일기를 찾을 수 없습니다: " + diarySeq));

        // 2. 같은 감정의 다른 일기들 조회
        List<Diary> sameCategoryDiaries = diaryRepository.findByUserSeqAndIsDeleted(diary.getUserSeq(), "N").stream()
                .filter(d -> !d.getIsDeleted().equals("Y"))
                .filter(d -> !d.getDiarySeq().equals(diarySeq))
                .filter(d -> Objects.equals(d.getEmotionSeq(), diary.getEmotionSeq()))
                .collect(Collectors.toList());

        if (sameCategoryDiaries.isEmpty()) {
            return Collections.emptyList();
        }

        // 3. 일기 간 연결 관계 계산
        Map<Integer, List<Integer>> connections = connectionService.optimizeConnections(
                Stream.concat(Stream.of(diary), sameCategoryDiaries.stream())
                        .collect(Collectors.toList())
        );

        // 4. 현재 일기의 연결 목록 반환
        List<Integer> connectedDiaries = connections.getOrDefault(diarySeq, Collections.emptyList());

        // 개수 제한 적용
        if (connectedDiaries.size() <= maxResults) {
            return connectedDiaries;
        }

        // 유사도 순으로 정렬하여 반환
        return connectedDiaries.stream()
                .limit(maxResults)
                .collect(Collectors.toList());
    }

    @Override
    public Map<Integer, List<Integer>> relayoutUniverse(Integer userSeq) {
        log.info("사용자 {} 우주 별자리 형태 재배치 시작", userSeq);

        // CoordinateResetService를 사용하여 전체 우주 재배치
        Map<Integer, List<Integer>> connections = coordinateResetService.resetEntireUniverse(userSeq);

        log.info("사용자 {} 우주 별자리 형태 재배치 완료. 연결 수: {}",
                userSeq, connections.values().stream().mapToInt(List::size).sum());

        return connections;
    }
}
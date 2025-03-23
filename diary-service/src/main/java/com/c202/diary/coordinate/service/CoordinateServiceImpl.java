package com.c202.diary.coordinate.service;

import com.c202.diary.coordinate.model.CoordinateDto;
import com.c202.diary.diary.entity.Diary;
import com.c202.diary.diary.repository.DiaryRepository;
import com.c202.diary.emotion.entity.Emotion;
import com.c202.diary.emotion.repository.EmotionRepository;
import com.c202.diary.tag.entity.DiaryTag;
import com.c202.diary.tag.entity.Tag;
import com.c202.diary.tag.repository.DiaryTagRepository;
import com.c202.diary.tag.repository.TagRepository;
import com.c202.exception.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CoordinateServiceImpl implements CoordinateService {

    private final EmotionRepository emotionRepository;
    private final DiaryRepository diaryRepository;
    private final DiaryTagRepository diaryTagRepository;
    private final TagRepository tagRepository;

    private final Random random = new Random();

    @Override
    public CoordinateDto generateCoordinates(String mainEmotion, List<String> tags, Integer diarySeq) {
        // 1. 감정 영역 정보 가져오기
        Emotion emotion = emotionRepository.findByName(mainEmotion)
                .orElseThrow(() -> new CustomException("존재하지 않는 감정입니다: " + mainEmotion));

        // 2. 같은 감정을 가진 일기 목록 조회
        List<Diary> emotionDiaries = diaryRepository.findAll().stream()
                .filter(d -> !d.getIsDeleted().equals("Y"))
                .filter(d -> Objects.equals(d.getX(), emotion.getBaseX()) &&
                        Objects.equals(d.getY(), emotion.getBaseY()) &&
                        Objects.equals(d.getZ(), emotion.getBaseZ()))
                .collect(Collectors.toList());

        // 3. 태그 유사성이 높은 일기 찾기
        Map<Integer, Double> similarityScores = new HashMap<>();

        if (diarySeq != null) {
            // 수정 시에는 자기 자신 제외
            emotionDiaries = emotionDiaries.stream()
                    .filter(d -> !d.getDiarySeq().equals(diarySeq))
                    .collect(Collectors.toList());
        }

        // 4. 태그 기반 유사도 계산
        for (Diary diary : emotionDiaries) {
            List<String> diaryTags = getDiaryTags(diary);
            double similarity = calculateTagSimilarity(tags, diaryTags);
            similarityScores.put(diary.getDiarySeq(), similarity);
        }

        // 5. 기준 위치 결정
        double baseX = emotion.getBaseX();
        double baseY = emotion.getBaseY();
        double baseZ = emotion.getBaseZ();

        // 6. 유사 일기가 있으면 그 주변에 배치, 없으면 감정 중심 주변에 배치
        double targetX, targetY, targetZ;

        if (!similarityScores.isEmpty()) {
            // 유사도가 가장 높은 일기 찾기
            Integer mostSimilarDiarySeq = Collections.max(similarityScores.entrySet(), Map.Entry.comparingByValue()).getKey();
            Diary mostSimilarDiary = diaryRepository.findByDiarySeq(mostSimilarDiarySeq)
                    .orElseThrow(() -> new CustomException("일기를 찾을 수 없습니다: " + mostSimilarDiarySeq));

            // 유사 일기 주변에 배치 (약간의 변동 추가)
            targetX = mostSimilarDiary.getX();
            targetY = mostSimilarDiary.getY();
            targetZ = mostSimilarDiary.getZ();
        } else {
            // 감정 중심 주변에 배치
            targetX = baseX;
            targetY = baseY;
            targetZ = baseZ;
        }

        // 7. 최종 좌표 계산 (구면 좌표계 활용)
        double radius = emotion.getBaseRadius() * 0.8 * (0.5 + random.nextDouble() * 0.5); // 반경의 50%~100% 범위 내
        double theta = random.nextDouble() * 2 * Math.PI; // 0~2π
        double phi = random.nextDouble() * Math.PI;       // 0~π

        // 목표 지점으로부터의 오프셋 계산
        double offsetX = radius * Math.sin(phi) * Math.cos(theta);
        double offsetY = radius * Math.sin(phi) * Math.sin(theta);
        double offsetZ = radius * Math.cos(phi);

        // 최종 좌표 (목표 지점 + 오프셋)
        double finalX = targetX + offsetX;
        double finalY = targetY + offsetY;
        double finalZ = targetZ + offsetZ;

        // 8. 기존 일기들과의 충돌 검사 및 조정
        finalX = adjustForCollisions(finalX, emotionDiaries, 0);
        finalY = adjustForCollisions(finalY, emotionDiaries, 1);
        finalZ = adjustForCollisions(finalZ, emotionDiaries, 2);

        // 9. 결과 반환
        return CoordinateDto.builder()
                .x(finalX)
                .y(finalY)
                .z(finalZ)
                .emotionSeq(emotion.getEmotionSeq())
                .emotionName(emotion.getName())
                .distance(calculateDistance(baseX, baseY, baseZ, finalX, finalY, finalZ))
                .build();
    }

    @Override
    public CoordinateDto updateCoordinates(Diary diary, String mainEmotion, List<String> tags) {
        // 대표 감정이 변경된 경우 새로운 좌표 생성, 그렇지 않으면 기존 좌표 주변에서 재계산
        Emotion currentEmotion = emotionRepository.findByEmotionSeq(diary.getEmotionSeq())
                .orElse(null);

        Emotion targetEmotion = emotionRepository.findByName(mainEmotion)
                .orElseThrow(() -> new CustomException("존재하지 않는 감정입니다: " + mainEmotion));

        // 대표 감정이 변경되었거나 현재 감정이 없는 경우 새로 생성
        if (currentEmotion == null || !currentEmotion.getName().equals(mainEmotion)) {
            return generateCoordinates(mainEmotion, tags, diary.getDiarySeq());
        }

        // 태그만 변경된 경우 좌표 미세 조정
        // 기존 좌표 기준으로 약간의 변화만 추가
        double adjustmentFactor = 0.2; // 조정 정도 (20%)
        double adjustX = (random.nextDouble() - 0.5) * 2 * adjustmentFactor;
        double adjustY = (random.nextDouble() - 0.5) * 2 * adjustmentFactor;
        double adjustZ = (random.nextDouble() - 0.5) * 2 * adjustmentFactor;

        // 기존 좌표에 조정값 적용
        double newX = diary.getX() + adjustX;
        double newY = diary.getY() + adjustY;
        double newZ = diary.getZ() + adjustZ;

        // 영역 내에 유지되도록 확인 (감정 중심 기준 반경 이내로)
        double distance = calculateDistance(targetEmotion.getBaseX(), targetEmotion.getBaseY(),
                targetEmotion.getBaseZ(), newX, newY, newZ);

        // 영역을 벗어나면 다시 조정
        if (distance > targetEmotion.getBaseRadius()) {
            double scale = targetEmotion.getBaseRadius() / distance * 0.95; // 약간의 여유 두기
            newX = targetEmotion.getBaseX() + (newX - targetEmotion.getBaseX()) * scale;
            newY = targetEmotion.getBaseY() + (newY - targetEmotion.getBaseY()) * scale;
            newZ = targetEmotion.getBaseZ() + (newZ - targetEmotion.getBaseZ()) * scale;
        }

        return CoordinateDto.builder()
                .x(newX)
                .y(newY)
                .z(newZ)
                .emotionSeq(targetEmotion.getEmotionSeq())
                .emotionName(targetEmotion.getName())
                .distance(calculateDistance(targetEmotion.getBaseX(), targetEmotion.getBaseY(),
                        targetEmotion.getBaseZ(), newX, newY, newZ))
                .build();
    }

    @Override
    public List<Integer> findSimilarDiaries(Integer diarySeq, int maxResults) {
        Diary diary = diaryRepository.findByDiarySeq(diarySeq)
                .orElseThrow(() -> new CustomException("일기를 찾을 수 없습니다: " + diarySeq));

        List<String> diaryTags = getDiaryTags(diary);

        // 감정이 같은 일기들 중에서 찾기
        List<Diary> sameCategoryDiaries = diaryRepository.findAll().stream()
                .filter(d -> !d.getIsDeleted().equals("Y"))
                .filter(d -> !d.getDiarySeq().equals(diarySeq))
                .filter(d -> Objects.equals(d.getEmotionSeq(), diary.getEmotionSeq()))
                .collect(Collectors.toList());

        // 태그 유사도 계산
        Map<Integer, Double> similarityScores = new HashMap<>();
        for (Diary otherDiary : sameCategoryDiaries) {
            List<String> otherTags = getDiaryTags(otherDiary);
            double similarity = calculateTagSimilarity(diaryTags, otherTags);
            similarityScores.put(otherDiary.getDiarySeq(), similarity);
        }

        // 유사도 순으로 정렬하여 상위 N개 반환
        return similarityScores.entrySet().stream()
                .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
                .limit(maxResults)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    // 일기의 태그 목록 가져오기
    private List<String> getDiaryTags(Diary diary) {
        List<DiaryTag> diaryTags = diaryTagRepository.findByDiary(diary);
        return diaryTags.stream()
                .map(diaryTag -> diaryTag.getTag().getName())
                .collect(Collectors.toList());
    }

    // 태그 유사도 계산 (자카드 유사도 사용)
    private double calculateTagSimilarity(List<String> tags1, List<String> tags2) {
        if (tags1.isEmpty() || tags2.isEmpty()) {
            return 0.0;
        }

        Set<String> union = new HashSet<>(tags1);
        union.addAll(tags2);

        Set<String> intersection = new HashSet<>(tags1);
        intersection.retainAll(tags2);

        return (double) intersection.size() / union.size();
    }

    // 거리 계산
    private double calculateDistance(double x1, double y1, double z1, double x2, double y2, double z2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2) + Math.pow(z2 - z1, 2));
    }

    // 충돌 방지를 위한 좌표 조정 (같은 좌표에 일기가 겹치지 않도록)
    private double adjustForCollisions(double coordinate, List<Diary> existingDiaries, int axis) {
        // 최소 거리 설정
        double minDistance = 1.0;

        boolean hasCollision;
        double adjustedCoordinate = coordinate;
        int maxAttempts = 10;

        do {
            hasCollision = false;

            for (Diary existingDiary : existingDiaries) {
                double existingCoordinate;

                if (axis == 0) existingCoordinate = existingDiary.getX();
                else if (axis == 1) existingCoordinate = existingDiary.getY();
                else existingCoordinate = existingDiary.getZ();

                // 충돌 확인
                if (Math.abs(adjustedCoordinate - existingCoordinate) < minDistance) {
                    hasCollision = true;

                    // 랜덤한 방향으로 조금 이동
                    double adjustment = minDistance * (1.0 + random.nextDouble() * 0.5);
                    if (random.nextBoolean()) {
                        adjustedCoordinate += adjustment;
                    } else {
                        adjustedCoordinate -= adjustment;
                    }

                    break;
                }
            }

            maxAttempts--;
        } while (hasCollision && maxAttempts > 0);

        return adjustedCoordinate;
    }
}
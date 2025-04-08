package com.c202.diary.util.coordinate.service;

import com.c202.diary.util.coordinate.model.CoordinateDto;
import com.c202.diary.diary.entity.Diary;
import com.c202.diary.diary.repository.DiaryRepository;
import com.c202.diary.emotion.entity.Emotion;
import com.c202.diary.emotion.repository.EmotionRepository;
import com.c202.diary.tag.entity.DiaryTag;
import com.c202.diary.tag.repository.DiaryTagRepository;
import com.c202.exception.types.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 일기의 3D 좌표 생성 및 관리를 담당하는 서비스
 * 별자리 형태의 좌표 생성과 연결 관계 설정을 통합 관리합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CoordinateServiceImpl implements CoordinateService {

    private final EmotionRepository emotionRepository;
    private final DiaryRepository diaryRepository;
    private final DiaryTagRepository diaryTagRepository;
    private final ConstellationLayoutService layoutService;
    private final ConstellationConnectionService connectionService;
    private final CoordinateResetService resetService;

    // 결과 캐싱을 위한 맵 (사용자별 마지막 계산 결과)
    private final Map<Integer, Map<Integer, List<Integer>>> userConnectionCache = new HashMap<>();
    private final Map<Integer, Long> userConnectionCacheTimestamp = new HashMap<>();

    // 캐시 유효 시간 (5분)
    private static final long CACHE_VALIDITY_MS = 5 * 60 * 1000;

    // 구의 반경 (전체 우주의 크기)
    private static final double SPHERE_RADIUS = 150.0;

    /**
     * 새 일기를 위한 좌표를 생성합니다.
     * 감정 영역과 태그 유사성에 기반하여 별자리 패턴에 맞는 좌표를 생성합니다.
     *
     * @param mainEmotion 주요 감정 이름
     * @param tags 태그 목록
     * @param diarySeq 일기 시퀀스 (새 일기라면 null)
     * @return 생성된 좌표 정보
     */
    @Override
    public CoordinateDto generateCoordinates(String mainEmotion, List<String> tags, Integer diarySeq) {
        try {
            // 1. 감정 정보 조회
            Emotion emotion = emotionRepository.findByName(mainEmotion)
                    .orElseThrow(() -> new NotFoundException("존재하지 않는 감정입니다: " + mainEmotion));

            // 2. 같은 감정의 다른 일기들 조회
            List<Diary> sameCategoryDiaries = diaryRepository.findAll().stream()
                    .filter(d -> !d.getIsDeleted().equals("Y"))
                    .filter(d -> d.getDiarySeq() != null && (diarySeq == null || !d.getDiarySeq().equals(diarySeq)))
                    .filter(d -> d.getEmotionSeq() != null && d.getEmotionSeq().equals(emotion.getEmotionSeq()))
                    .collect(Collectors.toList());

            // 3. 최적의 위치 결정
            double[] coordinates = findOptimalPosition(emotion, sameCategoryDiaries, tags);

            log.info("새 일기 좌표 생성 완료: emotion={}, x={}, y={}, z={}",
                    mainEmotion, coordinates[0], coordinates[1], coordinates[2]);

            return CoordinateDto.builder()
                    .x(coordinates[0])
                    .y(coordinates[1])
                    .z(coordinates[2])
                    .emotionSeq(emotion.getEmotionSeq())
                    .emotionName(emotion.getName())
                    .build();
        } catch (Exception e) {
            log.error("좌표 생성 중 오류 발생: {}", e.getMessage(), e);

            // 오류 발생 시 기본 좌표 제공 (구 표면의 랜덤 지점)
            double[] defaultCoordinates = generateRandomSphereCoordinates();

            Integer emotionSeq = null;
            try {
                emotionSeq = emotionRepository.findByName(mainEmotion)
                        .map(Emotion::getEmotionSeq).orElse(null);
            } catch (Exception ex) {
                log.error("감정 조회 중 추가 오류: {}", ex.getMessage());
            }

            return CoordinateDto.builder()
                    .x(defaultCoordinates[0])
                    .y(defaultCoordinates[1])
                    .z(defaultCoordinates[2])
                    .emotionSeq(emotionSeq)
                    .emotionName(mainEmotion)
                    .build();
        }
    }

    /**
     * 구 표면의 랜덤한 지점 좌표 생성
     */
    private double[] generateRandomSphereCoordinates() {
        Random random = new Random();

        // 구면 좌표계 사용
        double phi = Math.acos(2 * random.nextDouble() - 1); // 0 ~ PI
        double theta = random.nextDouble() * 2 * Math.PI;    // 0 ~ 2PI

        // 구면 좌표를 3D 직교 좌표로 변환
        return new double[] {
                SPHERE_RADIUS * Math.sin(phi) * Math.cos(theta),
                SPHERE_RADIUS * Math.sin(phi) * Math.sin(theta),
                SPHERE_RADIUS * Math.cos(phi)
        };
    }

    /**
     * 기존 일기의 좌표를 업데이트합니다.
     * 태그나 감정이 변경된 경우 새 위치를 계산합니다.
     *
     * @param diary 업데이트할 일기
     * @param mainEmotion 주요 감정 이름
     * @param tags 태그 목록
     * @return 업데이트된 좌표 정보
     */
    @Override
    @Transactional
    public CoordinateDto updateCoordinates(Diary diary, String mainEmotion, List<String> tags) {
        try {
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

                double[] coordinates = findOptimalPosition(targetEmotion, Collections.emptyList(), tags);

                // 일기 엔티티에도 좌표 적용
                diary.setCoordinates(coordinates[0], coordinates[1], coordinates[2], targetEmotion.getEmotionSeq());

                // 변경된 일기 저장
                diaryRepository.save(diary);

                return CoordinateDto.builder()
                        .x(coordinates[0])
                        .y(coordinates[1])
                        .z(coordinates[2])
                        .emotionSeq(targetEmotion.getEmotionSeq())
                        .emotionName(targetEmotion.getName())
                        .build();
            }

            // 3. 동일 감정이면 기존 좌표 유지
            // 기존 좌표가 null이면 신규 좌표 생성
            if (diary.getX() == null || diary.getY() == null || diary.getZ() == null) {
                log.info("기존 좌표가 null이어서 새 좌표 생성: diary={}, emotion={}", diary.getDiarySeq(), mainEmotion);

                double[] coordinates = findOptimalPosition(targetEmotion, Collections.emptyList(), tags);

                // 일기 엔티티에도 좌표 적용
                diary.setCoordinates(coordinates[0], coordinates[1], coordinates[2], targetEmotion.getEmotionSeq());

                // 변경된 일기 저장
                diaryRepository.save(diary);

                return CoordinateDto.builder()
                        .x(coordinates[0])
                        .y(coordinates[1])
                        .z(coordinates[2])
                        .emotionSeq(targetEmotion.getEmotionSeq())
                        .emotionName(targetEmotion.getName())
                        .build();
            }

            log.info("좌표 변경 없음: diary={}, emotion={}", diary.getDiarySeq(), mainEmotion);

            return CoordinateDto.builder()
                    .x(diary.getX())
                    .y(diary.getY())
                    .z(diary.getZ())
                    .emotionSeq(targetEmotion.getEmotionSeq())
                    .emotionName(targetEmotion.getName())
                    .build();
        } catch (Exception e) {
            log.error("좌표 업데이트 중 오류 발생: {}", e.getMessage(), e);

            // 오류 발생 시 기존 좌표 유지 (없으면 새 좌표 생성)
            double[] coordinates;
            if (diary.getX() != null && diary.getY() != null && diary.getZ() != null) {
                coordinates = new double[] {diary.getX(), diary.getY(), diary.getZ()};
            } else {
                coordinates = generateRandomSphereCoordinates();
            }

            Integer emotionSeq = null;
            try {
                emotionSeq = emotionRepository.findByName(mainEmotion)
                        .map(Emotion::getEmotionSeq).orElse(null);
            } catch (Exception ex) {
                log.error("감정 조회 중 추가 오류: {}", ex.getMessage());
            }

            return CoordinateDto.builder()
                    .x(coordinates[0])
                    .y(coordinates[1])
                    .z(coordinates[2])
                    .emotionSeq(emotionSeq)
                    .emotionName(mainEmotion)
                    .build();
        }
    }

    /**
     * 현재 일기와 유사한 다른 일기들을 찾습니다.
     * 태그 유사도와 감정에 기반하여 가장 관련있는 일기들을 반환합니다.
     *
     * @param diarySeq 기준 일기 시퀀스
     * @param maxResults 반환할 최대 결과 수
     * @return 유사한 일기 시퀀스 목록
     */
    @Override
    public List<Integer> findSimilarDiaries(Integer diarySeq, int maxResults) {
        try {
            // 1. 일기 조회
            Diary diary = diaryRepository.findByDiarySeq(diarySeq)
                    .orElseThrow(() -> new NotFoundException("일기를 찾을 수 없습니다: " + diarySeq));

            // 캐시 확인
            Integer userSeq = diary.getUserSeq();
            if (userConnectionCache.containsKey(userSeq)) {
                long lastUpdateTime = userConnectionCacheTimestamp.getOrDefault(userSeq, 0L);
                long currentTime = System.currentTimeMillis();

                // 캐시가 유효하면 캐시된 결과 사용
                if (currentTime - lastUpdateTime < CACHE_VALIDITY_MS) {
                    Map<Integer, List<Integer>> connections = userConnectionCache.get(userSeq);
                    if (connections.containsKey(diarySeq)) {
                        List<Integer> result = connections.get(diarySeq);
                        return result.stream().limit(maxResults).collect(Collectors.toList());
                    }
                }
            }

            // 2. 유사 일기 찾기
            // 기본 알고리즘: 같은 감정 + 태그 유사도
            List<Diary> candidateDiaries = diaryRepository.findByUserSeqAndIsDeleted(diary.getUserSeq(), "N").stream()
                    .filter(d -> !d.getIsDeleted().equals("Y"))
                    .filter(d -> !d.getDiarySeq().equals(diarySeq))
                    .filter(d -> Objects.equals(d.getEmotionSeq(), diary.getEmotionSeq()))
                    .collect(Collectors.toList());

            if (candidateDiaries.isEmpty()) {
                return Collections.emptyList();
            }

            // 태그 기반 유사도 계산
            List<String> diaryTags = getDiaryTags(diary);
            List<DiaryWithSimilarity> similarities = new ArrayList<>();

            for (Diary other : candidateDiaries) {
                List<String> otherTags = getDiaryTags(other);
                double similarity = calculateTagSimilarity(diaryTags, otherTags);
                similarities.add(new DiaryWithSimilarity(other, similarity));
            }

            // 유사도 기준 내림차순 정렬
            similarities.sort(Comparator.comparing(DiaryWithSimilarity::getSimilarity).reversed());

            // 가장 유사한 일기들 선택
            return similarities.stream()
                    .limit(maxResults)
                    .map(sim -> sim.getDiary().getDiarySeq())
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("유사 일기 검색 중 오류 발생: {}", e.getMessage(), e);
            return Collections.emptyList();
        }
    }

    /**
     * 사용자의 모든 일기를 재배치합니다.
     * 별자리 패턴에 맞게 감정별로 일기들을 재배치하고 연결 관계를 최적화합니다.
     *
     * @param userSeq 사용자 시퀀스
     * @return 일기 간 연결 관계 맵
     */
    @Override
    @Transactional
    public Map<Integer, List<Integer>> relayoutUniverse(Integer userSeq) {
        log.info("사용자 {} 우주 별자리 형태 재배치 시작", userSeq);

        try {
            // CoordinateResetService 호출하여 우주 재배치
            Map<Integer, List<Integer>> connections = resetService.resetEntireUniverse(userSeq);

            // 연결 관계 캐싱
            userConnectionCache.put(userSeq, connections);
            userConnectionCacheTimestamp.put(userSeq, System.currentTimeMillis());

            log.info("사용자 {} 우주 별자리 형태 재배치 완료. 연결 수: {}",
                    userSeq, connections.values().stream().mapToInt(List::size).sum() / 2);

            return connections;
        } catch (Exception e) {
            log.error("우주 재배치 중 오류 발생: {}", e.getMessage(), e);
            return new HashMap<>();
        }
    }

    /**
     * 태그와 감정에 기반하여 최적의 위치를 결정합니다.
     * 구 표면에 위치하는 좌표를 생성합니다.
     */
    private double[] findOptimalPosition(Emotion emotion, List<Diary> sameCategoryDiaries, List<String> tags) {
        // 감정 영역 내 위치 생성
        if (sameCategoryDiaries.isEmpty()) {
            // 같은 감정의 일기가 없으면 감정 영역 내 구 표면의 랜덤 위치
            return generatePositionInEmotionArea(emotion);
        }

        // 유사한 일기가 있으면 그 주변에 배치
        // 가중치 부여: 태그 유사도 (70%) + 랜덤 요소 (30%)

        // 1. 태그 유사도가 가장 높은 일기 찾기
        Diary mostSimilarDiary = null;
        double highestSimilarity = -1;

        for (Diary candidate : sameCategoryDiaries) {
            List<String> candidateTags = getDiaryTags(candidate);
            double similarity = calculateTagSimilarity(tags, candidateTags);

            if (similarity > highestSimilarity) {
                highestSimilarity = similarity;
                mostSimilarDiary = candidate;
            }
        }

        // 2. 유사한 일기 주변으로 위치 생성
        if (mostSimilarDiary != null && highestSimilarity > 0.2 &&
                mostSimilarDiary.getX() != null && mostSimilarDiary.getY() != null && mostSimilarDiary.getZ() != null) {

            // 유사 일기 위치 기반 좌표 계산
            return generatePositionNearDiary(mostSimilarDiary);
        }

        // 유사한 일기가 없거나 유사도가 낮으면 감정 영역 내 랜덤 위치
        return generatePositionInEmotionArea(emotion);
    }

    /**
     * 감정 영역 내 구 표면의 랜덤 위치 생성
     */
    private double[] generatePositionInEmotionArea(Emotion emotion) {
        Random random = new Random();

        // 감정 중심으로부터의 각도
        double phi = Math.acos(2 * random.nextDouble() - 1); // 0 ~ PI
        double theta = random.nextDouble() * 2 * Math.PI;    // 0 ~ 2PI

        // 감정 영역 내 위치 (감정 반경의 70~90% 사이)
        double radius = emotion.getBaseRadius() * (0.7 + 0.2 * random.nextDouble());

        // 방향 벡터
        double dirX = Math.sin(phi) * Math.cos(theta);
        double dirY = Math.sin(phi) * Math.sin(theta);
        double dirZ = Math.cos(phi);

        // 감정 중심 + 방향 * 반경
        double x = emotion.getBaseX() + dirX * radius;
        double y = emotion.getBaseY() + dirY * radius;
        double z = emotion.getBaseZ() + dirZ * radius;

        // 구 표면까지의 거리 계산
        double distance = Math.sqrt(x*x + y*y + z*z);

        // 구 표면에 배치 (필요한 경우)
        if (Math.abs(distance - SPHERE_RADIUS) > SPHERE_RADIUS * 0.1) {
            double factor = SPHERE_RADIUS / Math.max(distance, 0.001);
            x *= factor;
            y *= factor;
            z *= factor;
        }

        return new double[] {x, y, z};
    }

    /**
     * 기존 일기 근처에 새 위치 생성
     */
    private double[] generatePositionNearDiary(Diary diary) {
        Random random = new Random();

        // 기존 일기 좌표
        double x = diary.getX();
        double y = diary.getY();
        double z = diary.getZ();

        // 약간의 변형 추가 (5~15% 랜덤 이동)
        double variation = 0.05 + 0.1 * random.nextDouble();
        double angle1 = random.nextDouble() * 2 * Math.PI;
        double angle2 = random.nextDouble() * 2 * Math.PI;

        x += Math.sin(angle1) * Math.cos(angle2) * variation * SPHERE_RADIUS;
        y += Math.sin(angle1) * Math.sin(angle2) * variation * SPHERE_RADIUS;
        z += Math.cos(angle1) * variation * SPHERE_RADIUS;

        // 구 표면까지의 거리 계산
        double distance = Math.sqrt(x*x + y*y + z*z);

        // 구 표면에 배치 (필요한 경우)
        if (Math.abs(distance - SPHERE_RADIUS) > SPHERE_RADIUS * 0.1) {
            double factor = SPHERE_RADIUS / Math.max(distance, 0.001);
            x *= factor;
            y *= factor;
            z *= factor;
        }

        return new double[] {x, y, z};
    }

    /**
     * 일기의 태그 목록을 조회합니다.
     */
    private List<String> getDiaryTags(Diary diary) {
        if (diary == null) {
            return Collections.emptyList();
        }

        try {
            List<DiaryTag> diaryTags = diaryTagRepository.findByDiary(diary);
            return diaryTags.stream()
                    .map(diaryTag -> {
                        if (diaryTag.getTag() != null) {
                            return diaryTag.getTag().getName();
                        }
                        return null;
                    })
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("일기 태그 조회 중 오류: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 두 태그 목록 간의 자카드 유사도를 계산합니다.
     */
    private double calculateTagSimilarity(List<String> tags1, List<String> tags2) {
        if (tags1 == null) tags1 = Collections.emptyList();
        if (tags2 == null) tags2 = Collections.emptyList();

        if (tags1.isEmpty() && tags2.isEmpty()) {
            return 0.5; // 둘 다 태그 없으면 중간 유사도
        }

        if (tags1.isEmpty() || tags2.isEmpty()) {
            return 0.1; // 한쪽만 태그 있으면 낮은 유사도
        }

        Set<String> union = new HashSet<>(tags1);
        union.addAll(tags2);

        Set<String> intersection = new HashSet<>(tags1);
        intersection.retainAll(tags2);

        return (double) intersection.size() / union.size();
    }

    /**
     * 내부 클래스: 일기와 유사도 정보
     */
    private static class DiaryWithSimilarity {
        private final Diary diary;
        private final double similarity;

        public DiaryWithSimilarity(Diary diary, double similarity) {
            this.diary = diary;
            this.similarity = similarity;
        }

        public Diary getDiary() {
            return diary;
        }

        public double getSimilarity() {
            return similarity;
        }
    }
}
package com.c202.diary.util.coordinate.service;

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
 * 우주 좌표계를 별자리 형태로 재설정하는 서비스
 * 일기들을 감정별로 그룹화하고 별자리 패턴으로 배치합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CoordinateResetService {

    private final DiaryRepository diaryRepository;
    private final EmotionRepository emotionRepository;
    private final DiaryTagRepository diaryTagRepository;
    private final ConstellationLayoutService layoutService;
    private final ConstellationConnectionService connectionService;

    // 별자리당 최적 일기 수
    private static final int OPTIMAL_DIARIES_PER_CONSTELLATION = 5;

    // 별자리당 최대 일기 수
    private static final int MAX_DIARIES_PER_CONSTELLATION = 7;

    // 별자리당 최소 일기 수
    private static final int MIN_DIARIES_PER_CONSTELLATION = 3;

    // 구의 반경 (전체 우주의 크기)
    private static final double SPHERE_RADIUS = 150.0;

    /**
     * 전체 우주를 재배치합니다.
     * 모든 일기를 별자리 패턴으로 재배치하고 연결 관계를 최적화합니다.
     *
     * @param userSeq 사용자 시퀀스
     * @return 연결 관계 맵 (일기 ID → 연결된 일기 ID 목록)
     */
    @Transactional
    public Map<Integer, List<Integer>> resetEntireUniverse(Integer userSeq) {
        log.info("사용자 {} 우주 별자리 형태 재배치 시작", userSeq);

        // 1. 사용자의 모든 일기 조회
        List<Diary> diaries = diaryRepository.findByUserSeqAndIsDeleted(userSeq, "N");

        if (diaries.isEmpty()) {
            log.info("재배치할 일기가 없습니다. 사용자: {}", userSeq);
            return new HashMap<>();
        }

        // null 좌표 확인
        int nullCoordinatesCount = 0;
        for (Diary diary : diaries) {
            if (diary.getX() == null || diary.getY() == null || diary.getZ() == null) {
                nullCoordinatesCount++;
            }
        }
        log.info("사용자 {}의 전체 일기 수: {}, 좌표가 null인 일기 수: {}",
                userSeq, diaries.size(), nullCoordinatesCount);

        // 2. 감정별로 일기 그룹화 (감정이 null인 일기 제외)
        Map<Integer, List<Diary>> diariesByEmotion = diaries.stream()
                .filter(d -> d.getEmotionSeq() != null)
                .collect(Collectors.groupingBy(Diary::getEmotionSeq));

        // 3. 전체 연결 관계 맵
        Map<Integer, List<Integer>> allConnections = new HashMap<>();

        // 4. 각 감정별로 처리
        for (Map.Entry<Integer, List<Diary>> entry : diariesByEmotion.entrySet()) {
            Integer emotionSeq = entry.getKey();
            List<Diary> emotionDiaries = entry.getValue();

            try {
                // 감정 정보 조회
                Emotion emotion = emotionRepository.findById(emotionSeq)
                        .orElseThrow(() -> new NotFoundException("감정을 찾을 수 없습니다 (seq: " + emotionSeq + ")"));

                // 별자리 클러스터 그룹화 - 간소화된 로직 사용
                List<List<Diary>> constellationGroups = createConstellationGroups(emotionDiaries);

                log.info("감정 '{}' 클러스터링 결과: {} 그룹", emotion.getName(), constellationGroups.size());
                for (int i = 0; i < constellationGroups.size(); i++) {
                    log.debug("  - 그룹 {}: {} 일기", i + 1, constellationGroups.get(i).size());
                }

                // 각 별자리 좌표 생성 및 적용
                Map<Integer, double[][]> constellationCoordinates =
                        layoutService.generateMultipleConstellations(emotion, constellationGroups);

                // 좌표 적용
                applyCoordinatesToDiaries(constellationGroups, constellationCoordinates);

                // 별자리 연결 관계 생성
                Map<Integer, List<Integer>> emotionConnections =
                        connectionService.optimizeConstellationConnections(constellationGroups);

                // 전체 연결 관계에 추가
                allConnections.putAll(emotionConnections);
            } catch (Exception e) {
                log.error("감정 {} 처리 중 오류 발생: {}", emotionSeq, e.getMessage(), e);
            }
        }

        // 5. 감정이 null인 일기들에 대한 처리 (필요한 경우)
        handleDiariesWithoutEmotion(diaries.stream()
                .filter(d -> d.getEmotionSeq() == null)
                .collect(Collectors.toList()));

        // 6. 변경된 일기들 저장
        try {
            diaryRepository.saveAll(diaries);
            log.info("사용자 {} 일기 {} 개 좌표 저장 완료", userSeq, diaries.size());
        } catch (Exception e) {
            log.error("일기 저장 중 오류 발생: {}", e.getMessage(), e);
        }

        return allConnections;
    }

    /**
     * 동일 감정 내 일기들을 별자리 클러스터로 그룹화합니다.
     * 간소화된 로직으로 자연스러운 별자리 형성을 촉진합니다.
     *
     * @param diaries 동일 감정의 일기 목록
     * @return 별자리 클러스터 목록
     */
    private List<List<Diary>> createConstellationGroups(List<Diary> diaries) {
        if (diaries == null || diaries.isEmpty()) {
            return Collections.emptyList();
        }

        // 전체 일기 수가 최대 별자리 크기 이하면 하나의 그룹으로
        if (diaries.size() <= MAX_DIARIES_PER_CONSTELLATION) {
            return Collections.singletonList(new ArrayList<>(diaries));
        }

        // 최적의 그룹 수 계산 (각 그룹이 OPTIMAL_DIARIES_PER_CONSTELLATION에 가깝도록)
        int groupCount = Math.max(1, (int) Math.ceil((double) diaries.size() / OPTIMAL_DIARIES_PER_CONSTELLATION));

        // 그룹화 방식 결정
        // 작은 수의 일기는 태그 기반 그룹화, 많은 수의 일기는 시간 기반 그룹화
        if (diaries.size() <= 20) {
            return createTagBasedGroups(diaries, groupCount);
        } else {
            return createTimeBasedGroups(diaries, groupCount);
        }
    }

    /**
     * 태그 유사도에 기반한 그룹화 (더 간소화된 버전)
     */
    private List<List<Diary>> createTagBasedGroups(List<Diary> diaries, int targetGroupCount) {
        // 그룹 목록 초기화
        List<List<Diary>> groups = new ArrayList<>();

        // 처리된 일기 추적
        Set<Integer> processedDiaries = new HashSet<>();

        // 1. 시드 일기 선택
        List<Diary> seeds = selectDiverseSeeds(diaries, targetGroupCount);

        // 각 시드로 초기 그룹 생성
        for (Diary seed : seeds) {
            List<Diary> group = new ArrayList<>();
            group.add(seed);
            processedDiaries.add(seed.getDiarySeq());
            groups.add(group);
        }

        // 2. 나머지 일기들을 가장 적합한 그룹에 할당
        List<Diary> unassigned = diaries.stream()
                .filter(d -> !processedDiaries.contains(d.getDiarySeq()))
                .collect(Collectors.toList());

        for (Diary diary : unassigned) {
            int bestGroupIndex = 0;
            double bestSimilarity = -1;

            // 각 그룹과의 유사도 계산
            for (int i = 0; i < groups.size(); i++) {
                List<Diary> group = groups.get(i);

                // 그룹이 이미 최대 크기라면 스킵
                if (group.size() >= MAX_DIARIES_PER_CONSTELLATION) {
                    continue;
                }

                double similarity = calculateGroupSimilarity(diary, group);
                if (similarity > bestSimilarity) {
                    bestSimilarity = similarity;
                    bestGroupIndex = i;
                }
            }

            // 가장 유사한 그룹에 할당
            // 모든 그룹이 가득 찬 경우 새 그룹 생성
            if (groups.get(bestGroupIndex).size() < MAX_DIARIES_PER_CONSTELLATION) {
                groups.get(bestGroupIndex).add(diary);
            } else {
                List<Diary> newGroup = new ArrayList<>();
                newGroup.add(diary);
                groups.add(newGroup);
            }
        }

        // 3. 너무 작은 그룹 처리
        return mergeSmallGroups(groups);
    }

    /**
     * 시간(생성일) 기반 그룹화
     */
    private List<List<Diary>> createTimeBasedGroups(List<Diary> diaries, int targetGroupCount) {
        // 생성일 기준 정렬
        diaries.sort(Comparator.comparing(Diary::getCreatedAt, Comparator.nullsLast(Comparator.naturalOrder())));

        // 그룹 목록 초기화
        List<List<Diary>> groups = new ArrayList<>();

        // 그룹당 일기 수 계산
        int diariesPerGroup = (int) Math.ceil((double) diaries.size() / targetGroupCount);

        // 그룹 나누기
        for (int i = 0; i < diaries.size(); i += diariesPerGroup) {
            int end = Math.min(i + diariesPerGroup, diaries.size());
            groups.add(new ArrayList<>(diaries.subList(i, end)));
        }

        return groups;
    }

    /**
     * 다양한 시드 일기 선택 (가능한 서로 다른 태그를 가진 일기들)
     */
    private List<Diary> selectDiverseSeeds(List<Diary> diaries, int count) {
        if (diaries.size() <= count) {
            return new ArrayList<>(diaries);
        }

        List<Diary> seeds = new ArrayList<>();
        Set<String> usedTags = new HashSet<>();

        // 태그가 다양한 일기들 선택
        for (Diary diary : diaries) {
            List<String> diaryTags = getDiaryTags(diary);

            // 이 일기가 새로운 태그를 가지고 있는지 확인
            boolean hasNewTag = false;
            for (String tag : diaryTags) {
                if (!usedTags.contains(tag)) {
                    hasNewTag = true;
                    break;
                }
            }

            if (hasNewTag || seeds.size() < count / 2) {
                seeds.add(diary);
                usedTags.addAll(diaryTags);

                if (seeds.size() >= count) {
                    break;
                }
            }
        }

        // 필요한 수만큼 선택되지 않았다면 나머지는 랜덤 선택
        if (seeds.size() < count) {
            List<Diary> remaining = diaries.stream()
                    .filter(d -> !seeds.contains(d))
                    .collect(Collectors.toList());

            Collections.shuffle(remaining);

            for (Diary diary : remaining) {
                seeds.add(diary);
                if (seeds.size() >= count) {
                    break;
                }
            }
        }

        return seeds;
    }

    /**
     * 일기와 그룹 간의 유사도 계산 (간소화 버전)
     */
    private double calculateGroupSimilarity(Diary diary, List<Diary> group) {
        if (group.isEmpty()) {
            return 0.0;
        }

        List<String> diaryTags = getDiaryTags(diary);

        // 그룹 내 모든 태그 수집
        Set<String> groupTags = new HashSet<>();
        for (Diary groupDiary : group) {
            groupTags.addAll(getDiaryTags(groupDiary));
        }

        // 태그 유사도 계산 (자카드)
        if (diaryTags.isEmpty() && groupTags.isEmpty()) {
            return 0.5; // 둘 다 태그 없으면 중간 유사도
        }

        if (diaryTags.isEmpty() || groupTags.isEmpty()) {
            return 0.1; // 한쪽만 태그 있으면 낮은 유사도
        }

        Set<String> intersection = new HashSet<>(diaryTags);
        intersection.retainAll(groupTags);

        Set<String> union = new HashSet<>(diaryTags);
        union.addAll(groupTags);

        return (double) intersection.size() / union.size();
    }

    /**
     * 너무 작은 그룹들을 병합합니다.
     */
    private List<List<Diary>> mergeSmallGroups(List<List<Diary>> groups) {
        if (groups.size() <= 1) {
            return groups;
        }

        List<List<Diary>> result = new ArrayList<>();

        // 크기 순 정렬 (작은 것부터)
        groups.sort(Comparator.comparing(List::size));

        for (List<Diary> group : groups) {
            // 그룹이 최소 크기 이상이면 결과에 추가
            if (group.size() >= MIN_DIARIES_PER_CONSTELLATION) {
                result.add(group);
                continue;
            }

            // 작은 그룹은 가장 적합한 그룹에 병합
            if (result.isEmpty()) {
                result.add(group);
                continue;
            }

            // 가장 적합한(유사도 높은) 그룹 찾기
            int bestGroupIndex = 0;
            double bestSimilarity = -1;

            for (int i = 0; i < result.size(); i++) {
                List<Diary> existingGroup = result.get(i);

                // 병합 후 크기가 최대 크기를 넘지 않는지 확인
                if (existingGroup.size() + group.size() > MAX_DIARIES_PER_CONSTELLATION) {
                    continue;
                }

                double similarity = calculateGroupToGroupSimilarity(group, existingGroup);
                if (similarity > bestSimilarity) {
                    bestSimilarity = similarity;
                    bestGroupIndex = i;
                }
            }

            // 병합 가능한 그룹이 있으면 병합
            if (result.get(bestGroupIndex).size() + group.size() <= MAX_DIARIES_PER_CONSTELLATION) {
                result.get(bestGroupIndex).addAll(group);
            } else {
                // 병합할 수 없으면 새 그룹으로 추가
                result.add(group);
            }
        }

        return result;
    }

    /**
     * 두 그룹 간의 태그 유사도 계산
     */
    private double calculateGroupToGroupSimilarity(List<Diary> group1, List<Diary> group2) {
        if (group1.isEmpty() || group2.isEmpty()) {
            return 0.0;
        }

        // 각 그룹의 모든 태그 수집
        Set<String> tags1 = new HashSet<>();
        for (Diary diary : group1) {
            tags1.addAll(getDiaryTags(diary));
        }

        Set<String> tags2 = new HashSet<>();
        for (Diary diary : group2) {
            tags2.addAll(getDiaryTags(diary));
        }

        // 자카드 유사도 계산
        if (tags1.isEmpty() && tags2.isEmpty()) {
            return 0.5; // 둘 다 태그 없으면 중간 유사도
        }

        if (tags1.isEmpty() || tags2.isEmpty()) {
            return 0.1; // 한쪽만 태그 있으면 낮은 유사도
        }

        Set<String> intersection = new HashSet<>(tags1);
        intersection.retainAll(tags2);

        Set<String> union = new HashSet<>(tags1);
        union.addAll(tags2);

        return (double) intersection.size() / union.size();
    }

    /**
     * 좌표를 일기들에 적용합니다.
     * null 체크 및 예외 처리 추가
     */
    private void applyCoordinatesToDiaries(
            List<List<Diary>> constellationGroups,
            Map<Integer, double[][]> constellationCoordinates) {

        for (List<Diary> group : constellationGroups) {
            if (group.isEmpty()) {
                continue;
            }

            int groupKey = group.get(0).getDiarySeq();
            double[][] coordinates = constellationCoordinates.get(groupKey);

            if (coordinates == null) {
                log.warn("그룹 {}의 좌표가 null입니다", groupKey);
                continue;
            }

            if (coordinates.length < group.size()) {
                log.warn("좌표 배열 크기가 부족합니다. 그룹: {}, 좌표: {}, 일기: {}",
                        groupKey, coordinates.length, group.size());
                continue;
            }

            // 각 일기에 좌표 적용
            for (int i = 0; i < group.size(); i++) {
                try {
                    Diary diary = group.get(i);

                    // 좌표가 null인지 확인
                    if (coordinates[i] == null || coordinates[i].length < 3) {
                        log.warn("일기 {}의 좌표가 null이거나 불완전합니다", diary.getDiarySeq());
                        continue;
                    }

                    diary.setCoordinate(coordinates[i][0], coordinates[i][1], coordinates[i][2]);


                    log.debug("일기 {} 좌표 설정: ({}, {}, {})",
                            diary.getDiarySeq(), diary.getX(), diary.getY(), diary.getZ());
                } catch (Exception e) {
                    log.error("일기 좌표 설정 중 오류: {}", e.getMessage());
                }
            }
        }
    }

    /**
     * 감정이 설정되지 않은 일기들 처리
     * 이 일기들은 구 표면의 랜덤한 위치에 배치
     */
    private void handleDiariesWithoutEmotion(List<Diary> diariesWithoutEmotion) {
        if (diariesWithoutEmotion.isEmpty()) {
            return;
        }

        log.info("감정이 없는 일기 {} 개 처리", diariesWithoutEmotion.size());

        // 감정이 없는 일기들은 구 표면의 랜덤한 위치에 배치
        Random random = new Random();

        for (Diary diary : diariesWithoutEmotion) {
            try {
                // 구면 좌표계 사용하여 구 표면에 균등하게 분포
                double phi = Math.acos(2 * random.nextDouble() - 1); // 0 ~ PI
                double theta = random.nextDouble() * 2 * Math.PI;    // 0 ~ 2PI

                // 구면 좌표를 3D 직교 좌표로 변환
                diary.setCoordinate(SPHERE_RADIUS * Math.sin(phi) * Math.cos(theta), SPHERE_RADIUS * Math.sin(phi) * Math.sin(theta), SPHERE_RADIUS * Math.cos(phi));

                log.debug("감정 없는 일기 {} 좌표 설정: ({}, {}, {})",
                        diary.getDiarySeq(), diary.getX(), diary.getY(), diary.getZ());
            } catch (Exception e) {
                log.error("감정 없는 일기 좌표 설정 중 오류: {}", e.getMessage());
            }
        }
    }

    /**
     * 일기의 태그 목록 조회 (null 안전)
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
}
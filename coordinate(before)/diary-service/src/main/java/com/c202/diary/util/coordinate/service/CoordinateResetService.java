package com.c202.diary.util.coordinate.service;

import com.c202.diary.diary.entity.Diary;
import com.c202.diary.diary.repository.DiaryRepository;
import com.c202.diary.emotion.entity.Emotion;
import com.c202.diary.emotion.repository.EmotionRepository;
import com.c202.diary.tag.entity.DiaryTag;
import com.c202.diary.tag.repository.DiaryTagRepository;
import com.c202.diary.util.coordinate.model.CoordinateDto;
import com.c202.exception.types.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

// 우주 좌표계를 별자리 형태로 재설정하는 서비스
// 일기들을 감정별로 그룹화하고 별자리 패턴으로 배치합니다.
@Slf4j
@Service
@RequiredArgsConstructor
public class CoordinateResetService {

    private final DiaryRepository diaryRepository;
    private final EmotionRepository emotionRepository;
    private final DiaryTagRepository diaryTagRepository;
    private final ConstellationLayoutService layoutService;
    private final ConstellationConnectionService connectionService;

    // 별자리 클러스터의 최소 크기 (너무 작은 클러스터는 의미 없음)
    private static final int MIN_CLUSTER_SIZE = 3;

    // 별자리 클러스터의 최대 크기 (너무 큰 클러스터는 복잡해짐)
    private static final int MAX_CLUSTER_SIZE = 7;

    // 태그 유사도 기준 클러스터링 임계값
    private static final double CLUSTERING_SIMILARITY_THRESHOLD = 0.3;

    // 전체 우주를 재배치합니다.
    // 모든 일기를 별자리 패턴으로 재배치하고 연결 관계를 최적화합니다.
    // @param userSeq 사용자 시퀀스
    // @return 연결 관계 맵 (일기 ID → 연결된 일기 ID 목록)
    public Map<Integer, List<Integer>> resetEntireUniverse(Integer userSeq) {
        // 사용자의 모든 일기 조회
        List<Diary> diaries = diaryRepository.findByUserSeqAndIsDeleted(userSeq, "N");

        if (diaries.isEmpty()) {
            log.info("재배치할 일기가 없습니다. 사용자: {}", userSeq);
            return new HashMap<>();
        }

        // 감정별로 일기 그룹화
        Map<Integer, List<Diary>> diariesByEmotion = groupDiariesByEmotion(diaries);

        // 각 감정별로 별자리 클러스터 생성 및 좌표 설정
        Map<Integer, List<Integer>> allConnections = new HashMap<>();

        for (Integer emotionSeq : diariesByEmotion.keySet()) {
            List<Diary> emotionDiaries = diariesByEmotion.get(emotionSeq);

            // 감정 정보 조회
            Emotion emotion = emotionRepository.findById(emotionSeq)
                    .orElseThrow(() -> new NotFoundException("감정을 찾을 수 없습니다 (seq: " + emotionSeq + ")"));

            // 별자리 클러스터 그룹화
            List<List<Diary>> constellationGroups = clusterIntoConstellations(emotionDiaries);

            // 각 별자리 좌표 생성
            Map<Integer, double[][]> constellationCoordinates =
                    layoutService.generateMultipleConstellations(emotion, constellationGroups);

            // 좌표 적용
            applyCoordinatesToDiaries(constellationGroups, constellationCoordinates);

            // 별자리 연결 관계 생성
            Map<Integer, List<Integer>> emotionConnections =
                    connectionService.optimizeConstellationConnections(constellationGroups);

            // 전체 연결 관계에 추가
            allConnections.putAll(emotionConnections);
        }

        // 변경된 일기들 저장
        diaryRepository.saveAll(diaries);

        return allConnections;
    }

    // 일기를 감정별로 그룹화합니다.
    // @param diaries 전체 일기 목록
    // @return 감정별 일기 목록 맵
    private Map<Integer, List<Diary>> groupDiariesByEmotion(List<Diary> diaries) {
        return diaries.stream()
                .filter(d -> d.getEmotionSeq() != null)
                .collect(Collectors.groupingBy(Diary::getEmotionSeq));
    }

    // 동일 감정 내 일기들을 별자리 클러스터로 그룹화합니다.
    // 태그 유사도를 기준으로 비슷한 일기들을 하나의 별자리로 묶습니다.
    // @param diaries 동일 감정의 일기 목록
    // @return 별자리 클러스터 목록
    public List<List<Diary>> clusterIntoConstellations(List<Diary> diaries) {
        List<List<Diary>> clusters = new ArrayList<>();
        Set<Integer> assignedDiaries = new HashSet<>();

        // 일기 수가 적으면 하나의 클러스터로 처리
        if (diaries.size() <= MAX_CLUSTER_SIZE) {
            clusters.add(new ArrayList<>(diaries));
            return clusters;
        }

        // 유사도 기반 클러스터링
        for (Diary diary : diaries) {
            if (assignedDiaries.contains(diary.getDiarySeq())) {
                continue;
            }

            List<Diary> cluster = new ArrayList<>();
            cluster.add(diary);
            assignedDiaries.add(diary.getDiarySeq());

            List<String> diaryTags = getDiaryTags(diary);

            // 유사도가 높은 일기들 추가
            List<DiaryWithSimilarity> similarities = new ArrayList<>();
            for (Diary otherDiary : diaries) {
                if (otherDiary.getDiarySeq().equals(diary.getDiarySeq()) ||
                        assignedDiaries.contains(otherDiary.getDiarySeq())) {
                    continue;
                }

                List<String> otherTags = getDiaryTags(otherDiary);
                double similarity = calculateTagSimilarity(diaryTags, otherTags);

                if (similarity >= CLUSTERING_SIMILARITY_THRESHOLD) {
                    similarities.add(new DiaryWithSimilarity(otherDiary, similarity));
                }
            }

            // 유사도 높은 순으로 정렬
            similarities.sort(Comparator.comparing(DiaryWithSimilarity::getSimilarity).reversed());

            // 클러스터 크기 제한 내에서 추가
            for (DiaryWithSimilarity sim : similarities) {
                if (cluster.size() >= MAX_CLUSTER_SIZE) {
                    break;
                }
                cluster.add(sim.getDiary());
                assignedDiaries.add(sim.getDiary().getDiarySeq());
            }

            clusters.add(cluster);
        }

        // 아직 할당되지 않은 일기들 처리
        List<Diary> unassigned = diaries.stream()
                .filter(d -> !assignedDiaries.contains(d.getDiarySeq()))
                .collect(Collectors.toList());

        if (!unassigned.isEmpty()) {
            // 작은 클러스터들 먼저 채우기
            for (Diary diary : unassigned) {
                // 가장 작은 클러스터 찾기
                List<Diary> smallestCluster = clusters.stream()
                        .filter(c -> c.size() < MAX_CLUSTER_SIZE)
                        .min(Comparator.comparing(List::size))
                        .orElse(null);

                if (smallestCluster != null) {
                    smallestCluster.add(diary);
                } else {
                    // 모든 클러스터가 가득 찼다면 새 클러스터 생성
                    List<Diary> newCluster = new ArrayList<>();
                    newCluster.add(diary);
                    clusters.add(newCluster);
                }
            }
        }

        // 너무 작은 클러스터들 병합
        mergeSmallClusters(clusters);

        return clusters;
    }

    // 너무 작은 클러스터들을 병합합니다.
    // @param clusters 클러스터 목록
    private void mergeSmallClusters(List<List<Diary>> clusters) {
        if (clusters.size() <= 1) {
            return;
        }

        boolean mergeOccurred;
        do {
            mergeOccurred = false;

            // 크기 순으로 정렬 (작은 것부터)
            clusters.sort(Comparator.comparing(List::size));

            List<Diary> smallestCluster = clusters.get(0);
            if (smallestCluster.size() < MIN_CLUSTER_SIZE) {
                // 병합 대상 찾기 (가장 유사한 다른 클러스터)
                int bestTargetIdx = -1;
                double bestSimilarity = -1;

                for (int i = 1; i < clusters.size(); i++) {
                    List<Diary> targetCluster = clusters.get(i);

                    // 병합 후 크기가 최대 크기를 넘지 않는지 확인
                    if (smallestCluster.size() + targetCluster.size() <= MAX_CLUSTER_SIZE) {
                        double similarity = calculateClusterSimilarity(smallestCluster, targetCluster);
                        if (similarity > bestSimilarity) {
                            bestSimilarity = similarity;
                            bestTargetIdx = i;
                        }
                    }
                }

                // 병합 가능한 대상이 있으면 병합
                if (bestTargetIdx != -1) {
                    clusters.get(bestTargetIdx).addAll(smallestCluster);
                    clusters.remove(0);
                    mergeOccurred = true;
                } else {
                    // 병합할 대상이 없으면 종료
                    break;
                }
            } else {
                // 가장 작은 클러스터도 최소 크기 이상이면 종료
                break;
            }
        } while (mergeOccurred && clusters.size() > 1);
    }

    // 생성된 좌표를 일기들에 적용합니다.
    // @param constellationGroups 별자리 그룹 목록
    // @param constellationCoordinates 그룹별 좌표 맵
    private void applyCoordinatesToDiaries(
            List<List<Diary>> constellationGroups,
            Map<Integer, double[][]> constellationCoordinates) {

        for (List<Diary> group : constellationGroups) {
            if (group.isEmpty()) {
                continue;
            }

            int groupKey = group.get(0).getDiarySeq();
            double[][] coordinates = constellationCoordinates.get(groupKey);

            if (coordinates == null || coordinates.length < group.size()) {
                log.warn("좌표 배열이 없거나 크기가 부족합니다. 그룹: {}, 좌표: {}",
                        groupKey, coordinates != null ? coordinates.length : "null");
                continue;
            }

            // 각 일기에 좌표 적용
            for (int i = 0; i < group.size(); i++) {
                Diary diary = group.get(i);
                diary.setCoordinates(
                        coordinates[i][0],
                        coordinates[i][1],
                        coordinates[i][2],
                        diary.getEmotionSeq()
                );
            }
        }
    }

    // 단일 일기에 대한 최적의 좌표를 생성합니다.
    // 새 일기를 추가할 때 사용됩니다.
    // @param diary 일기
    // @return 생성된 좌표
    public CoordinateDto generateOptimalCoordinates(Diary diary) {
        if (diary.getEmotionSeq() == null) {
            throw new IllegalArgumentException("일기에 감정이 설정되어 있지 않습니다.");
        }

        // 같은 감정의 다른 일기들 조회
        List<Diary> sameCategoryDiaries = diaryRepository.findAll().stream()
                .filter(d -> !d.getIsDeleted().equals("Y"))
                .filter(d -> !d.getDiarySeq().equals(diary.getDiarySeq()))
                .filter(d -> Objects.equals(d.getEmotionSeq(), diary.getEmotionSeq()))
                .collect(Collectors.toList());

        // 감정 정보 조회
        Emotion emotion = emotionRepository.findById(diary.getEmotionSeq())
                .orElseThrow(() -> new NotFoundException("감정을 찾을 수 없습니다."));

        // 태그 유사도 기반으로 가장 가까운 별자리 찾기
        List<String> diaryTags = getDiaryTags(diary);
        List<List<Diary>> constellationGroups = clusterIntoConstellations(sameCategoryDiaries);

        // 가장 유사한 클러스터 찾기
        List<Diary> bestCluster = null;
        double bestSimilarity = -1;

        for (List<Diary> cluster : constellationGroups) {
            double similarity = calculateClusterTagSimilarity(cluster, diaryTags);
            if (similarity > bestSimilarity) {
                bestSimilarity = similarity;
                bestCluster = cluster;
            }
        }

        // 가장 유사한 클러스터를 찾지 못했거나, 유사도가 낮으면 새 클러스터 생성
        if (bestCluster == null || bestSimilarity < CLUSTERING_SIMILARITY_THRESHOLD) {
            // 새 별자리 패턴의 좌표 생성
            double[][] templatePositions = layoutService.generateConstellationLayout(
                    emotion, Collections.singletonList(diary));

            return CoordinateDto.builder()
                    .x(templatePositions[0][0])
                    .y(templatePositions[0][1])
                    .z(templatePositions[0][2])
                    .emotionSeq(emotion.getEmotionSeq())
                    .emotionName(emotion.getName())
                    .build();
        }

        // 기존 클러스터에 추가
        bestCluster.add(diary);

        // 클러스터 재배치
        double[][] coordinates = layoutService.generateConstellationLayout(emotion, bestCluster);

        // 새 일기의 좌표 (마지막 인덱스)
        int newDiaryIndex = bestCluster.size() - 1;

        return CoordinateDto.builder()
                .x(coordinates[newDiaryIndex][0])
                .y(coordinates[newDiaryIndex][1])
                .z(coordinates[newDiaryIndex][2])
                .emotionSeq(emotion.getEmotionSeq())
                .emotionName(emotion.getName())
                .build();
    }

    // 두 클러스터 간의 유사도를 계산합니다.
    // @param cluster1 첫 번째 클러스터
    // @param cluster2 두 번째 클러스터
    // @return 유사도
    private double calculateClusterSimilarity(List<Diary> cluster1, List<Diary> cluster2) {
        if (cluster1.isEmpty() || cluster2.isEmpty()) {
            return 0.0;
        }

        // 각 클러스터의 모든 태그를 모음
        Set<String> tags1 = new HashSet<>();
        Set<String> tags2 = new HashSet<>();

        for (Diary diary : cluster1) {
            tags1.addAll(getDiaryTags(diary));
        }

        for (Diary diary : cluster2) {
            tags2.addAll(getDiaryTags(diary));
        }

        // 자카드 유사도 계산
        Set<String> union = new HashSet<>(tags1);
        union.addAll(tags2);

        if (union.isEmpty()) {
            return 0.1; // 기본 유사도
        }

        Set<String> intersection = new HashSet<>(tags1);
        intersection.retainAll(tags2);

        return (double) intersection.size() / union.size();
    }

    // 클러스터와 태그 목록 간의 유사도를 계산합니다.
    // @param cluster 클러스터
    // @param tags 태그 목록
    // @return 유사도
    private double calculateClusterTagSimilarity(List<Diary> cluster, List<String> tags) {
        if (cluster.isEmpty() || tags.isEmpty()) {
            return 0.0;
        }

        // 클러스터의 모든 태그를 모음
        Set<String> clusterTags = new HashSet<>();
        for (Diary diary : cluster) {
            clusterTags.addAll(getDiaryTags(diary));
        }

        // 자카드 유사도 계산
        Set<String> union = new HashSet<>(clusterTags);
        union.addAll(tags);

        if (union.isEmpty()) {
            return 0.1; // 기본 유사도
        }

        Set<String> intersection = new HashSet<>(clusterTags);
        intersection.retainAll(tags);

        return (double) intersection.size() / union.size();
    }

    // 일기의 태그 목록을 조회합니다.
    // @param diary 일기
    // @return 태그 이름 목록
    private List<String> getDiaryTags(Diary diary) {
        List<DiaryTag> diaryTags = diaryTagRepository.findByDiary(diary);
        return diaryTags.stream()
                .map(diaryTag -> diaryTag.getTag().getName())
                .collect(Collectors.toList());
    }

    // 두 태그 목록 간의 자카드 유사도를 계산합니다.
    // @param tags1 첫 번째 태그 목록
    // @param tags2 두 번째 태그 목록
    // @return 유사도 (0~1)
    private double calculateTagSimilarity(List<String> tags1, List<String> tags2) {
        if (tags1.isEmpty() && tags2.isEmpty()) {
            return 0.1; // 기본 유사도
        }

        if (tags1.isEmpty() || tags2.isEmpty()) {
            return 0.0;
        }

        Set<String> union = new HashSet<>(tags1);
        union.addAll(tags2);

        Set<String> intersection = new HashSet<>(tags1);
        intersection.retainAll(tags2);

        return (double) intersection.size() / union.size();
    }

    // 내부 클래스: 일기와 유사도를 함께 저장
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
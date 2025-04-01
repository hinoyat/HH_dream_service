package com.c202.diary.util.coordinate.service;

import com.c202.diary.util.coordinate.model.ClusterDto;
import com.c202.diary.util.coordinate.model.CoordinateDto;
import com.c202.diary.diary.entity.Diary;
import com.c202.diary.diary.repository.DiaryRepository;
import com.c202.diary.emotion.entity.Emotion;
import com.c202.diary.emotion.repository.EmotionRepository;
import com.c202.diary.tag.entity.DiaryTag;
import com.c202.diary.tag.repository.DiaryTagRepository;
import com.c202.exception.types.*;
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

    private final Random random = new Random();

    // 클러스터링 임계값
    private static final double CLUSTER_SIMILARITY_THRESHOLD = 0.3;
    // MST 알고리즘에서 사용될 연결 가중치 최대값
    private static final double MAX_CONNECTION_WEIGHT = 100.0;
    // 최대 연결 수 상수 정의
    private static final int DEFAULT_MAX_CONNECTIONS_PER_DIARY = 3;

    @Override
    public CoordinateDto generateCoordinates(String mainEmotion, List<String> tags, Integer diarySeq) {
        // 1. 감정 영역 정보 가져오기
        Emotion emotion = emotionRepository.findByName(mainEmotion)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 감정입니다: " + mainEmotion));

        // 2. 감정 영역 내 일기 목록 조회
        List<Diary> emotionDiaries = diaryRepository.findAll().stream()
                .filter(d -> !d.getIsDeleted().equals("Y"))
                .filter(d -> d.getEmotionSeq() != null && d.getEmotionSeq().equals(emotion.getEmotionSeq()))
                .collect(Collectors.toList());

        // 수정 시에는 자기 자신 제외
        if (diarySeq != null) {
            emotionDiaries = emotionDiaries.stream()
                    .filter(d -> !d.getDiarySeq().equals(diarySeq))
                    .collect(Collectors.toList());
        }

        // 3. 감정 영역 내 클러스터 형성
        List<ClusterDto> clusters = formClusters(emotionDiaries, tags);

        // 4. 최적의 클러스터 선택 또는 새 클러스터 생성
        ClusterDto targetCluster;
        if (clusters.isEmpty()) {
            // 새 클러스터 생성
            targetCluster = createNewCluster(emotion);
        } else {
            // 태그 유사성이 가장 높은 클러스터 선택
            targetCluster = findBestCluster(clusters, tags);
        }

        // 5. 클러스터 내에서 좌표 생성
        double[] coordinates = generateCoordinatesInCluster(targetCluster, emotion);

        // 6. 충돌 방지 및 좌표 조정
        List<double[]> existingCoordinates = emotionDiaries.stream()
                .map(d -> new double[]{d.getX(), d.getY(), d.getZ()})
                .collect(Collectors.toList());
        coordinates = adjustCoordinates(coordinates, existingCoordinates);

        return CoordinateDto.builder()
                .x(coordinates[0])
                .y(coordinates[1])
                .z(coordinates[2])
                .emotionSeq(emotion.getEmotionSeq())
                .emotionName(emotion.getName())
                .distance(calculateDistance(emotion.getBaseX(), emotion.getBaseY(),
                        emotion.getBaseZ(), coordinates[0], coordinates[1], coordinates[2]))
                .build();
    }

    @Override
    public CoordinateDto updateCoordinates(Diary diary, String mainEmotion, List<String> tags) {
        // 대표 감정이 변경된 경우 새로운 좌표 생성, 그렇지 않으면 기존 좌표 주변에서 재계산
        Emotion currentEmotion = emotionRepository.findByEmotionSeq(diary.getEmotionSeq())
                .orElse(null);

        Emotion targetEmotion = emotionRepository.findByName(mainEmotion)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 감정입니다: " + mainEmotion));

        // 대표 감정이 변경되었거나 현재 감정이 없는 경우 새로 생성
        if (currentEmotion == null || !currentEmotion.getName().equals(mainEmotion)) {
            return generateCoordinates(mainEmotion, tags, diary.getDiarySeq());
        }

        // 같은 감정 내에서 태그만 변경된 경우
        // 기존 클러스터에서의 위치를 유지하면서 약간의 조정만 수행

        // 1. 현재 감정 내 다른 일기들 조회
        List<Diary> emotionDiaries = diaryRepository.findAll().stream()
                .filter(d -> !d.getIsDeleted().equals("Y"))
                .filter(d -> !d.getDiarySeq().equals(diary.getDiarySeq()))
                .filter(d -> d.getEmotionSeq() != null && d.getEmotionSeq().equals(targetEmotion.getEmotionSeq()))
                .collect(Collectors.toList());

        // 2. 클러스터 형성
        List<ClusterDto> clusters = formClusters(emotionDiaries, tags);

        // 3. 태그 유사성이 가장 높은 클러스터 찾기
        ClusterDto targetCluster;
        if (clusters.isEmpty()) {
            // 새 클러스터 생성 - 하지만 기존 위치 근처에 배치
            targetCluster = new ClusterDto();
            targetCluster.setCenterX(diary.getX());
            targetCluster.setCenterY(diary.getY());
            targetCluster.setCenterZ(diary.getZ());
        } else {
            // 태그 유사성이 가장 높은 클러스터 선택
            targetCluster = findBestCluster(clusters, tags);
        }

        // 4. 조정 정도 설정 - 태그 변경에 따라 조정 정도 달라짐
        double adjustmentFactor = 0.2; // 기본 조정 정도 (20%)

        // 기존 좌표와 클러스터 중심 간의 거리 계산
        double distToClusterCenter = calculateDistance(
                diary.getX(), diary.getY(), diary.getZ(),
                targetCluster.getCenterX(), targetCluster.getCenterY(), targetCluster.getCenterZ());

        // 5. 새 좌표 계산 - 기존 위치와 새 클러스터 간의 절충
        double[] newCoordinates = new double[3];

        // 기존 위치에서 클러스터 중심 방향으로 약간 이동
        if (distToClusterCenter > 0) {
            // 이동 방향 벡터 계산
            double dirX = (targetCluster.getCenterX() - diary.getX()) / distToClusterCenter;
            double dirY = (targetCluster.getCenterY() - diary.getY()) / distToClusterCenter;
            double dirZ = (targetCluster.getCenterZ() - diary.getZ()) / distToClusterCenter;

            // 이동 거리 계산 (거리의 일부만 이동)
            double moveDistance = distToClusterCenter * adjustmentFactor;

            // 새 좌표 계산
            newCoordinates[0] = diary.getX() + dirX * moveDistance;
            newCoordinates[1] = diary.getY() + dirY * moveDistance;
            newCoordinates[2] = diary.getZ() + dirZ * moveDistance;
        } else {
            // 클러스터 중심과 일치하는 경우 약간 랜덤하게 이동
            double randomOffset = 5.0;
            newCoordinates[0] = diary.getX() + (random.nextDouble() - 0.5) * 2 * randomOffset;
            newCoordinates[1] = diary.getY() + (random.nextDouble() - 0.5) * 2 * randomOffset;
            newCoordinates[2] = diary.getZ() + (random.nextDouble() - 0.5) * 2 * randomOffset;
        }

        // 6. 감정 영역 내에 있는지 확인하고 조정
        double distanceFromEmotionCenter = calculateDistance(
                targetEmotion.getBaseX(), targetEmotion.getBaseY(), targetEmotion.getBaseZ(),
                newCoordinates[0], newCoordinates[1], newCoordinates[2]
        );

        if (distanceFromEmotionCenter > targetEmotion.getBaseRadius()) {
            // 영역을 벗어난 경우 감정 영역 내로 조정
            double scale = targetEmotion.getBaseRadius() / distanceFromEmotionCenter * 0.95;
            newCoordinates[0] = targetEmotion.getBaseX() + (newCoordinates[0] - targetEmotion.getBaseX()) * scale;
            newCoordinates[1] = targetEmotion.getBaseY() + (newCoordinates[1] - targetEmotion.getBaseY()) * scale;
            newCoordinates[2] = targetEmotion.getBaseZ() + (newCoordinates[2] - targetEmotion.getBaseZ()) * scale;
        }

        // 7. 충돌 방지 및 좌표 조정
        List<double[]> existingCoordinates = emotionDiaries.stream()
                .map(d -> new double[]{d.getX(), d.getY(), d.getZ()})
                .collect(Collectors.toList());
        newCoordinates = adjustCoordinates(newCoordinates, existingCoordinates);

        return CoordinateDto.builder()
                .x(newCoordinates[0])
                .y(newCoordinates[1])
                .z(newCoordinates[2])
                .emotionSeq(targetEmotion.getEmotionSeq())
                .emotionName(targetEmotion.getName())
                .distance(calculateDistance(targetEmotion.getBaseX(), targetEmotion.getBaseY(),
                        targetEmotion.getBaseZ(), newCoordinates[0], newCoordinates[1], newCoordinates[2]))
                .build();
    }

    @Override
    public List<Integer> findSimilarDiaries(Integer diarySeq, int maxResults) {
        Diary diary = diaryRepository.findByDiarySeq(diarySeq)
                .orElseThrow(() -> new NotFoundException("일기를 찾을 수 없습니다: " + diarySeq));

        List<String> diaryTags = getDiaryTags(diary);

        // 감정이 같은 일기들 중에서 찾기
        List<Diary> sameCategoryDiaries = diaryRepository.findAll().stream()
                .filter(d -> !d.getIsDeleted().equals("Y"))
                .filter(d -> !d.getDiarySeq().equals(diarySeq))
                .filter(d -> Objects.equals(d.getEmotionSeq(), diary.getEmotionSeq()))
                .collect(Collectors.toList());

        // MST 알고리즘 적용
        Map<Integer, List<Integer>> mst = calculateMST(diary, sameCategoryDiaries);

        // 결과 확인
        if (mst.containsKey(diarySeq)) {
            List<Integer> connections = mst.get(diarySeq);
            if (connections.size() <= maxResults) {
                return connections;
            }
            // maxResults 개수만큼 유사도가 높은 일기만 반환
            Map<Integer, Double> similarityScores = new HashMap<>();
            for (Integer connectedDiarySeq : connections) {
                Diary connectedDiary = sameCategoryDiaries.stream()
                        .filter(d -> d.getDiarySeq().equals(connectedDiarySeq))
                        .findFirst()
                        .orElse(null);

                if (connectedDiary != null) {
                    List<String> connectedTags = getDiaryTags(connectedDiary);
                    similarityScores.put(connectedDiarySeq, calculateTagSimilarity(diaryTags, connectedTags));
                }
            }

            return similarityScores.entrySet().stream()
                    .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
                    .limit(maxResults)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
        }

        // MST에 포함되지 않은 경우 (예외 상황) - 이전 유사도 기반 방식으로 대체
        Map<Integer, Double> similarityScores = new HashMap<>();
        for (Diary otherDiary : sameCategoryDiaries) {
            List<String> otherTags = getDiaryTags(otherDiary);
            similarityScores.put(otherDiary.getDiarySeq(), calculateTagSimilarity(diaryTags, otherTags));
        }

        return similarityScores.entrySet().stream()
                .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
                .limit(maxResults)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }

    // 감정 영역 내 일기들을 태그 유사성에 따라 클러스터링
    private List<ClusterDto> formClusters(List<Diary> diaries, List<String> newDiaryTags) {
        if (diaries.isEmpty()) {
            return new ArrayList<>();
        }

        // 클러스터 목록
        List<ClusterDto> clusters = new ArrayList<>();

        // 이미 클러스터에 할당된 일기 추적
        Set<Integer> assignedDiaries = new HashSet<>();

        // 각 일기를 순회하며 클러스터 형성
        for (Diary diary : diaries) {
            if (assignedDiaries.contains(diary.getDiarySeq())) {
                continue;
            }

            // 새 클러스터 시작
            ClusterDto cluster = new ClusterDto();
            cluster.getDiaries().add(diary);

            List<String> diaryTags = getDiaryTags(diary);

            // 클러스터에 유사한 다른 일기 추가
            for (Diary otherDiary : diaries) {
                if (otherDiary.getDiarySeq().equals(diary.getDiarySeq()) ||
                        assignedDiaries.contains(otherDiary.getDiarySeq())) {
                    continue;
                }

                List<String> otherTags = getDiaryTags(otherDiary);
                double similarity = calculateTagSimilarity(diaryTags, otherTags);

                // 유사도가 임계값 이상이면 클러스터에 추가
                if (similarity >= CLUSTER_SIMILARITY_THRESHOLD) {
                    cluster.getDiaries().add(otherDiary);
                    assignedDiaries.add(otherDiary.getDiarySeq());
                }
            }

            // 클러스터 중심점 계산
            calculateClusterCenter(cluster);

            // 새 일기와의 유사도 계산
            cluster.setSimilarityToNewDiary(calculateTagSimilarity(
                    cluster.getDiaries().stream()
                            .flatMap(d -> getDiaryTags(d).stream())
                            .distinct()
                            .collect(Collectors.toList()),
                    newDiaryTags
            ));

            clusters.add(cluster);
            assignedDiaries.add(diary.getDiarySeq());
        }

        return clusters;
    }


    // 클러스터 중심점 계산
    private void calculateClusterCenter(ClusterDto cluster) {
        double sumX = 0, sumY = 0, sumZ = 0;
        int count = 0;

        for (Diary diary : cluster.getDiaries()) {
            if (diary.getX() != null && diary.getY() != null && diary.getZ() != null) {
                sumX += diary.getX();
                sumY += diary.getY();
                sumZ += diary.getZ();
                count++;
            }
        }

        if (count > 0) {
            cluster.setCenterX(sumX / count);
            cluster.setCenterY(sumY / count);
            cluster.setCenterZ(sumZ / count);
        }
    }


    // 새 클러스터 생성

    private ClusterDto createNewCluster(Emotion emotion) {
        ClusterDto cluster = new ClusterDto();

        // 감정 영역 내에서 랜덤한 위치에 클러스터 중심 설정
        double radius = emotion.getBaseRadius() * 0.7 * (0.5 + random.nextDouble() * 0.5);
        double theta = random.nextDouble() * 2 * Math.PI;
        double phi = random.nextDouble() * Math.PI;

        double offsetX = radius * Math.sin(phi) * Math.cos(theta);
        double offsetY = radius * Math.sin(phi) * Math.sin(theta);
        double offsetZ = radius * Math.cos(phi);

        cluster.setCenterX(emotion.getBaseX() + offsetX);
        cluster.setCenterY(emotion.getBaseY() + offsetY);
        cluster.setCenterZ(emotion.getBaseZ() + offsetZ);

        return cluster;
    }


    // 태그와 가장 유사한 클러스터 찾기

    private ClusterDto findBestCluster(List<ClusterDto> clusters, List<String> tags) {
        return clusters.stream()
                .max(Comparator.comparing(ClusterDto::getSimilarityToNewDiary))
                .orElse(clusters.get(0));
    }


    // 클러스터 내에서 좌표 생성

    private double[] generateCoordinatesInCluster(ClusterDto cluster, Emotion emotion) {
        // 클러스터 중심으로부터의 거리 설정 (클러스터 크기에 비례)
        double clusterRadius = 10.0; // 기본 클러스터 반경

        // 클러스터 내 일기 수에 따라 반경 조정
        if (!cluster.getDiaries().isEmpty()) {
            clusterRadius = Math.min(20.0, 5.0 + 2.0 * cluster.getDiaries().size());
        }

        // 클러스터 내 랜덤 위치 계산
        double distance = clusterRadius * random.nextDouble();
        double theta = random.nextDouble() * 2 * Math.PI;
        double phi = random.nextDouble() * Math.PI;

        double offsetX = distance * Math.sin(phi) * Math.cos(theta);
        double offsetY = distance * Math.sin(phi) * Math.sin(theta);
        double offsetZ = distance * Math.cos(phi);

        double[] coordinates = new double[3];
        coordinates[0] = cluster.getCenterX() + offsetX;
        coordinates[1] = cluster.getCenterY() + offsetY;
        coordinates[2] = cluster.getCenterZ() + offsetZ;

        // 감정 영역 내에 있는지 확인하고 조정
        double distanceFromEmotionCenter = calculateDistance(
                emotion.getBaseX(), emotion.getBaseY(), emotion.getBaseZ(),
                coordinates[0], coordinates[1], coordinates[2]
        );

        if (distanceFromEmotionCenter > emotion.getBaseRadius()) {
            // 영역을 벗어난 경우 감정 영역 내로 조정
            double scale = emotion.getBaseRadius() / distanceFromEmotionCenter * 0.95;
            coordinates[0] = emotion.getBaseX() + (coordinates[0] - emotion.getBaseX()) * scale;
            coordinates[1] = emotion.getBaseY() + (coordinates[1] - emotion.getBaseY()) * scale;
            coordinates[2] = emotion.getBaseZ() + (coordinates[2] - emotion.getBaseZ()) * scale;
        }

        return coordinates;
    }


    // 충돌 방지를 위한 좌표 조정

    private double[] adjustCoordinates(double[] coordinates, List<double[]> existingCoordinates) {
        double minDistance = 5.0;
        int maxAttempts = 10;

        double[] adjustedCoordinates = coordinates.clone();

        for (int attempt = 0; attempt < maxAttempts; attempt++) {
            boolean hasCollision = false;

            for (double[] existing : existingCoordinates) {
                double distance = calculateDistance(
                        adjustedCoordinates[0], adjustedCoordinates[1], adjustedCoordinates[2],
                        existing[0], existing[1], existing[2]
                );

                if (distance < minDistance) {
                    hasCollision = true;

                    // 충돌 방지를 위해 약간 이동
                    double moveDistance = minDistance - distance + 1.0;

                    // 랜덤 방향으로 이동
                    double theta = random.nextDouble() * 2 * Math.PI;
                    double phi = random.nextDouble() * Math.PI;

                    adjustedCoordinates[0] += moveDistance * Math.sin(phi) * Math.cos(theta);
                    adjustedCoordinates[1] += moveDistance * Math.sin(phi) * Math.sin(theta);
                    adjustedCoordinates[2] += moveDistance * Math.cos(phi);

                    break;
                }
            }

            if (!hasCollision) {
                break;
            }
        }

        return adjustedCoordinates;
    }




    // MST(최소 스패닝 트리) 알고리즘을 적용하여 일기 간 연결 계산
    private Map<Integer, List<Integer>> calculateMST(Diary centerDiary, List<Diary> diaries) {
        List<Diary> allDiaries = new ArrayList<>(diaries);
        allDiaries.add(0, centerDiary); // 기준 일기를 첫 번째 위치에 추가

        int n = allDiaries.size();

        // 거리 행렬 계산 (유사도의 역수로 계산)
        double[][] distMatrix = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    distMatrix[i][j] = Double.MAX_VALUE; // 자기 자신과의 거리는 무한대
                } else {
                    // 태그 유사도 계산
                    double similarity = calculateTagSimilarity(
                            getDiaryTags(allDiaries.get(i)),
                            getDiaryTags(allDiaries.get(j))
                    );

                    // 유사도가 높을수록 거리가 가까움 (유사도의 역수)
                    // 유사도가 0일 경우 최대 가중치 부여
                    distMatrix[i][j] = similarity > 0 ? 1.0 / similarity : MAX_CONNECTION_WEIGHT;
                }
            }
        }

        // 프림 알고리즘으로 MST 계산
        boolean[] inMST = new boolean[n];
        double[] key = new double[n];
        int[] parent = new int[n];

        Arrays.fill(key, Double.MAX_VALUE);
        Arrays.fill(parent, -1);

        key[0] = 0; // 중심 일기를 시작점으로 선택

        for (int count = 0; count < n; count++) {
            // 아직 MST에 포함되지 않은 일기 중 가장 가까운 일기 선택
            int u = -1;
            double minKey = Double.MAX_VALUE;
            for (int i = 0; i < n; i++) {
                if (!inMST[i] && key[i] < minKey) {
                    minKey = key[i];
                    u = i;
                }
            }

            // 선택된 일기가 없으면 (그래프가 연결되지 않은 경우) 종료
            if (u == -1) break;

            inMST[u] = true;

            // 선택된 일기와 연결된 다른 일기들의 key 값 업데이트
            for (int v = 0; v < n; v++) {
                if (!inMST[v] && distMatrix[u][v] < key[v]) {
                    parent[v] = u;
                    key[v] = distMatrix[u][v];
                }
            }
        }

        // MST 결과를 연결 맵으로 변환
        Map<Integer, List<Integer>> connections = new HashMap<>();
        for (int i = 0; i < n; i++) {
            connections.put(allDiaries.get(i).getDiarySeq(), new ArrayList<>());
        }

        for (int i = 1; i < n; i++) { // 0번째는 루트이므로 parent가 없음
            if (parent[i] != -1) {
                int diary1Seq = allDiaries.get(parent[i]).getDiarySeq();
                int diary2Seq = allDiaries.get(i).getDiarySeq();

                // 양방향 연결 추가
                connections.get(diary1Seq).add(diary2Seq);
                connections.get(diary2Seq).add(diary1Seq);
            }
        }

// 연결 수 제한 적용
        return limitConnectionsInMST(connections, centerDiary, diaries, DEFAULT_MAX_CONNECTIONS_PER_DIARY);
    }

    // 거리 계산
    private double calculateDistance(double x1, double y1, double z1, double x2, double y2, double z2) {
        return Math.sqrt(Math.pow(x2 - x1, 2) + Math.pow(y2 - y1, 2) + Math.pow(z2 - z1, 2));
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

    // MST 계산 후 연결 수 제한 로직을 포함한 메서드
    private Map<Integer, List<Integer>> limitConnectionsInMST(Map<Integer, List<Integer>> mstConnections,
                                                              Diary centerDiary,
                                                              List<Diary> diaries,
                                                              int maxConnectionsPerDiary) {
        // 결과를 저장할 새 맵 생성
        Map<Integer, List<Integer>> limitedConnections = new HashMap<>();

        // 모든 일기 목록 생성 (중심 일기 포함)
        List<Diary> allDiaries = new ArrayList<>(diaries);
        allDiaries.add(0, centerDiary);

        // 각 일기별로 연결 제한 적용
        for (Diary diary : allDiaries) {
            Integer diarySeq = diary.getDiarySeq();
            List<Integer> connections = mstConnections.getOrDefault(diarySeq, new ArrayList<>());

            // 이미 연결 수가 제한 이하인 경우 그대로 사용
            if (connections.size() <= maxConnectionsPerDiary) {
                limitedConnections.put(diarySeq, new ArrayList<>(connections));
                continue;
            }

            // 연결이 제한보다 많은 경우, 유사도에 따라 정렬하여 상위 n개만 유지
            Map<Integer, Double> similarityScores = new HashMap<>();
            for (Integer connectedDiarySeq : connections) {
                // 연결된 일기 찾기
                Diary connectedDiary = allDiaries.stream()
                        .filter(d -> d.getDiarySeq().equals(connectedDiarySeq))
                        .findFirst()
                        .orElse(null);

                if (connectedDiary != null) {
                    // 태그 유사도 계산
                    double similarity = calculateTagSimilarity(
                            getDiaryTags(diary),
                            getDiaryTags(connectedDiary)
                    );
                    similarityScores.put(connectedDiarySeq, similarity);
                }
            }

            // 유사도가 높은 순으로 제한된 수의 연결만 유지
            List<Integer> limitedConnectionsList = similarityScores.entrySet().stream()
                    .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
                    .limit(maxConnectionsPerDiary)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());

            limitedConnections.put(diarySeq, limitedConnectionsList);
        }

        return limitedConnections;
    }
}
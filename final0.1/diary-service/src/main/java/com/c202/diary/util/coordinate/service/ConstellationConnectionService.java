package com.c202.diary.util.coordinate.service;

import com.c202.diary.diary.entity.Diary;
import com.c202.diary.tag.entity.DiaryTag;
import com.c202.diary.tag.repository.DiaryTagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * 별자리 연결 관계를 생성하고 최적화하는 서비스
 * 일기 간의 연결을 생성하여 자연스러운 별자리 모양을 형성합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConstellationConnectionService {

    private final DiaryTagRepository diaryTagRepository;

    // 일기별 최대 연결 수 (시각적으로 복잡해지지 않도록 제한)
    private static final int MAX_CONNECTIONS_PER_DIARY = 3;

    // 최소 연결 수 (모든 일기가 최소한 이 개수만큼은 연결되도록)
    private static final int MIN_CONNECTIONS_PER_DIARY = 1;

    // 3D 공간에서 최대 연결 거리 (너무 멀리 떨어진 일기는 연결하지 않음)
    private static final double MAX_CONNECTION_DISTANCE = 40.0;

    // 구의 반경 (전체 우주의 크기)
    private static final double SPHERE_RADIUS = 150.0;

    /**
     * 일기 그룹들에 대한 연결 관계를 최적화합니다.
     * 각 그룹은 하나의 별자리를 형성하고, 그룹 간에도 일부 연결됩니다.
     *
     * @param constellationGroups 별자리 그룹 목록
     * @return 최적화된 연결 관계
     */
    public Map<Integer, List<Integer>> optimizeConstellationConnections(List<List<Diary>> constellationGroups) {
        Map<Integer, List<Integer>> connections = new HashMap<>();

        // 모든 일기 초기화
        for (List<Diary> group : constellationGroups) {
            for (Diary diary : group) {
                connections.put(diary.getDiarySeq(), new ArrayList<>());
            }
        }

        // 각 별자리 그룹 내부의 연결 생성
        for (List<Diary> group : constellationGroups) {
            createConstellationConnections(group, connections);
        }

        // 별자리 그룹 간의 연결 생성 (필요한 경우)
        if (constellationGroups.size() > 1) {
            connectBetweenConstellations(constellationGroups, connections);
        }

        // 고립된 일기가 없도록 보장
        ensureMinimalConnections(connections, constellationGroups);

        return connections;
    }

    /**
     * 단일 일기 그룹(별자리)에 대한 연결 관계를 생성합니다.
     * 별자리 모양에 맞는 자연스러운 연결을 생성합니다.
     *
     * @param diaries 일기 그룹
     * @param connections 연결 관계 맵
     */
    public void createConstellationConnections(List<Diary> diaries, Map<Integer, List<Integer>> connections) {
        int size = diaries.size();
        if (size <= 1) return;

        // 일기가 적은 경우(2-3개)는 모두 연결
        if (size <= 3) {
            for (int i = 0; i < size; i++) {
                for (int j = i + 1; j < size; j++) {
                    Diary diary1 = diaries.get(i);
                    Diary diary2 = diaries.get(j);

                    // 거리 확인 - 너무 멀리 떨어진 경우 연결 안 함
                    if (calculateDistance(diary1, diary2) <= MAX_CONNECTION_DISTANCE) {
                        addConnection(connections, diary1.getDiarySeq(), diary2.getDiarySeq());
                    }
                }
            }
            return;
        }

        // 일기가 많은 경우는 패턴 기반 연결 방식 적용
        // 여기서는 MST(Minimum Spanning Tree) + 약간의 추가 연결 방식 사용

        // 1. 모든 가능한 엣지와 거리 계산
        List<DiaryEdge> edges = new ArrayList<>();
        for (int i = 0; i < size; i++) {
            for (int j = i + 1; j < size; j++) {
                Diary diary1 = diaries.get(i);
                Diary diary2 = diaries.get(j);
                double distance = calculateDistance(diary1, diary2);

                // 너무 멀리 떨어진 경우 제외
                if (distance <= MAX_CONNECTION_DISTANCE) {
                    edges.add(new DiaryEdge(diary1.getDiarySeq(), diary2.getDiarySeq(), distance));
                }
            }
        }

        // 거리순 정렬
        edges.sort(Comparator.comparingDouble(DiaryEdge::getDistance));

        // 2. MST 알고리즘으로 기본 연결 생성 (모든 일기가 연결되면서 총 거리는 최소화)
        Map<Integer, Integer> diaryToSet = new HashMap<>();
        for (Diary diary : diaries) {
            diaryToSet.put(diary.getDiarySeq(), diary.getDiarySeq());
        }

        for (DiaryEdge edge : edges) {
            int set1 = findSet(diaryToSet, edge.getDiary1Seq());
            int set2 = findSet(diaryToSet, edge.getDiary2Seq());

            if (set1 != set2) {
                // 서로 다른 집합이면 연결 추가
                addConnection(connections, edge.getDiary1Seq(), edge.getDiary2Seq());

                // 집합 병합
                diaryToSet.put(set1, set2);

                // 모든 일기가 같은 집합에 속하는지 확인
                boolean allConnected = true;
                int firstSet = findSet(diaryToSet, diaries.get(0).getDiarySeq());
                for (Diary diary : diaries) {
                    if (findSet(diaryToSet, diary.getDiarySeq()) != firstSet) {
                        allConnected = false;
                        break;
                    }
                }

                if (allConnected) break;
            }
        }

        // 3. 추가적인 연결 생성 (별자리 모양에 자연스러움 추가)
        // MST는 트리 구조라 사이클이 없으므로, 일부 추가 연결로 별자리 모양 향상
        for (DiaryEdge edge : edges) {
            int diary1Seq = edge.getDiary1Seq();
            int diary2Seq = edge.getDiary2Seq();

            // 이미 연결되어 있지 않고, 최대 연결 수를 초과하지 않는 경우
            if (!connections.get(diary1Seq).contains(diary2Seq) &&
                    connections.get(diary1Seq).size() < MAX_CONNECTIONS_PER_DIARY &&
                    connections.get(diary2Seq).size() < MAX_CONNECTIONS_PER_DIARY) {

                // 태그 유사도가 높거나 가까운 거리에 있는 일기 연결
                double similarity = calculateTagSimilarity(
                        getDiaryTags(diaries.stream().filter(d -> d.getDiarySeq().equals(diary1Seq)).findFirst().orElse(null)),
                        getDiaryTags(diaries.stream().filter(d -> d.getDiarySeq().equals(diary2Seq)).findFirst().orElse(null))
                );

                // 가까운 거리(하위 30%)이거나 유사도가 높으면(0.3 이상) 연결 추가
                if (edge.getDistance() < MAX_CONNECTION_DISTANCE * 0.3 || similarity > 0.3) {
                    addConnection(connections, diary1Seq, diary2Seq);
                }
            }
        }
    }

    /**
     * Union-Find 알고리즘을 위한 집합 찾기
     */
    private int findSet(Map<Integer, Integer> diaryToSet, int diarySeq) {
        if (diaryToSet.get(diarySeq) != diarySeq) {
            diaryToSet.put(diarySeq, findSet(diaryToSet, diaryToSet.get(diarySeq)));
        }
        return diaryToSet.get(diarySeq);
    }

    /**
     * 별자리 그룹 간 연결을 생성합니다.
     * 서로 다른 그룹의 일기 중 가장 가까운 것들을 연결합니다.
     *
     * @param constellationGroups 별자리 그룹 목록
     * @param connections 연결 관계 맵
     */
    private void connectBetweenConstellations(List<List<Diary>> constellationGroups, Map<Integer, List<Integer>> connections) {
        // 그룹이 하나면 연결할 필요 없음
        if (constellationGroups.size() <= 1) return;

        // 각 그룹의 중심점 계산
        Map<Integer, double[]> groupCenters = new HashMap<>();
        List<Integer> groupIds = new ArrayList<>();

        for (List<Diary> group : constellationGroups) {
            if (group.isEmpty()) continue;

            int groupId = group.get(0).getDiarySeq();
            groupIds.add(groupId);

            // 그룹 중심 계산
            double sumX = 0, sumY = 0, sumZ = 0;
            for (Diary diary : group) {
                sumX += diary.getX();
                sumY += diary.getY();
                sumZ += diary.getZ();
            }

            double[] center = {
                    sumX / group.size(),
                    sumY / group.size(),
                    sumZ / group.size()
            };

            groupCenters.put(groupId, center);
        }

        // MST 알고리즘으로 그룹 간 연결 최적화
        List<GroupEdge> groupEdges = new ArrayList<>();

        // 모든 그룹 쌍에 대한 거리 계산
        for (int i = 0; i < groupIds.size(); i++) {
            for (int j = i + 1; j < groupIds.size(); j++) {
                int group1Id = groupIds.get(i);
                int group2Id = groupIds.get(j);

                double[] center1 = groupCenters.get(group1Id);
                double[] center2 = groupCenters.get(group2Id);

                double distance = Math.sqrt(
                        Math.pow(center1[0] - center2[0], 2) +
                                Math.pow(center1[1] - center2[1], 2) +
                                Math.pow(center1[2] - center2[2], 2)
                );

                groupEdges.add(new GroupEdge(group1Id, group2Id, distance));
            }
        }

        // 거리순 정렬
        groupEdges.sort(Comparator.comparingDouble(GroupEdge::getDistance));

        // MST로 그룹 간 기본 연결 구조 생성
        Map<Integer, Integer> groupToSet = new HashMap<>();
        for (Integer groupId : groupIds) {
            groupToSet.put(groupId, groupId);
        }

        List<GroupEdge> selectedEdges = new ArrayList<>();

        for (GroupEdge edge : groupEdges) {
            int set1 = findGroupSet(groupToSet, edge.getGroup1Id());
            int set2 = findGroupSet(groupToSet, edge.getGroup2Id());

            if (set1 != set2) {
                selectedEdges.add(edge);
                groupToSet.put(set1, set2);
            }
        }

        // 선택된 그룹 간 연결에 대해, 각 그룹에서 가장 가까운 일기 쌍을 찾아 연결
        for (GroupEdge edge : selectedEdges) {
            List<Diary> group1 = constellationGroups.stream()
                    .filter(g -> !g.isEmpty() && g.get(0).getDiarySeq().equals(edge.getGroup1Id()))
                    .findFirst().orElse(new ArrayList<>());

            List<Diary> group2 = constellationGroups.stream()
                    .filter(g -> !g.isEmpty() && g.get(0).getDiarySeq().equals(edge.getGroup2Id()))
                    .findFirst().orElse(new ArrayList<>());

            if (group1.isEmpty() || group2.isEmpty()) continue;

            // 두 그룹 간 가장 가까운 일기 쌍 찾기
            Diary closest1 = null;
            Diary closest2 = null;
            double minDistance = Double.MAX_VALUE;

            for (Diary d1 : group1) {
                for (Diary d2 : group2) {
                    double dist = calculateDistance(d1, d2);
                    if (dist < minDistance &&
                            connections.get(d1.getDiarySeq()).size() < MAX_CONNECTIONS_PER_DIARY &&
                            connections.get(d2.getDiarySeq()).size() < MAX_CONNECTIONS_PER_DIARY) {
                        minDistance = dist;
                        closest1 = d1;
                        closest2 = d2;
                    }
                }
            }

            // 가장 가까운 일기 쌍 연결
            if (closest1 != null && closest2 != null) {
                addConnection(connections, closest1.getDiarySeq(), closest2.getDiarySeq());
            }
        }
    }

    /**
     * Union-Find 알고리즘을 위한 그룹 집합 찾기
     */
    private int findGroupSet(Map<Integer, Integer> groupToSet, int groupId) {
        if (groupToSet.get(groupId) != groupId) {
            groupToSet.put(groupId, findGroupSet(groupToSet, groupToSet.get(groupId)));
        }
        return groupToSet.get(groupId);
    }

    /**
     * 모든 일기가 최소한 하나의 연결을 가지도록 보장합니다.
     *
     * @param connections 연결 관계 맵
     * @param constellationGroups 별자리 그룹 목록
     */
    private void ensureMinimalConnections(Map<Integer, List<Integer>> connections, List<List<Diary>> constellationGroups) {
        // 모든 일기 목록 생성
        List<Diary> allDiaries = constellationGroups.stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());

        // 연결이 없는 일기 찾기
        for (Diary diary : allDiaries) {
            int diarySeq = diary.getDiarySeq();

            if (connections.get(diarySeq).isEmpty()) {
                // 가장 가까운 다른 일기 찾기
                Diary closest = null;
                double minDistance = Double.MAX_VALUE;

                for (Diary other : allDiaries) {
                    if (other.getDiarySeq().equals(diarySeq)) continue;

                    double distance = calculateDistance(diary, other);

                    if (distance < minDistance && distance <= MAX_CONNECTION_DISTANCE &&
                            connections.get(other.getDiarySeq()).size() < MAX_CONNECTIONS_PER_DIARY) {
                        minDistance = distance;
                        closest = other;
                    }
                }

                // 가장 가까운 일기와 연결
                if (closest != null) {
                    addConnection(connections, diarySeq, closest.getDiarySeq());
                    log.debug("고립된 일기 연결 추가: {} - {}", diarySeq, closest.getDiarySeq());
                } else {
                    log.warn("고립된 일기를 연결할 수 없음: {}", diarySeq);
                }
            }
        }
    }

    /**
     * 두 일기 간의 3D 거리를 계산합니다.
     */
    private double calculateDistance(Diary diary1, Diary diary2) {
        return Math.sqrt(
                Math.pow(diary1.getX() - diary2.getX(), 2) +
                        Math.pow(diary1.getY() - diary2.getY(), 2) +
                        Math.pow(diary1.getZ() - diary2.getZ(), 2)
        );
    }

    /**
     * 두 일기 간 양방향 연결 추가
     */
    private void addConnection(Map<Integer, List<Integer>> connections, int seq1, int seq2) {
        if (!connections.get(seq1).contains(seq2)) {
            connections.get(seq1).add(seq2);
        }

        if (!connections.get(seq2).contains(seq1)) {
            connections.get(seq2).add(seq1);
        }
    }

    /**
     * 일기의 태그 목록 조회
     */
    private List<String> getDiaryTags(Diary diary) {
        if (diary == null) return Collections.emptyList();
        List<DiaryTag> diaryTags = diaryTagRepository.findByDiary(diary);
        return diaryTags.stream()
                .map(diaryTag -> diaryTag.getTag().getName())
                .collect(Collectors.toList());
    }

    /**
     * 두 태그 목록 간 자카드 유사도 계산
     */
    private double calculateTagSimilarity(List<String> tags1, List<String> tags2) {
        if (tags1.isEmpty() && tags2.isEmpty()) {
            return 0.0;
        }

        if (tags1.isEmpty() || tags2.isEmpty()) {
            return 0.1; // 기본 유사도
        }

        Set<String> union = new HashSet<>(tags1);
        union.addAll(tags2);

        Set<String> intersection = new HashSet<>(tags1);
        intersection.retainAll(tags2);

        return (double) intersection.size() / union.size();
    }

    /**
     * 내부 클래스: 두 일기 간의 연결 정보
     */
    private static class DiaryEdge {
        private final int diary1Seq;
        private final int diary2Seq;
        private final double distance;

        public DiaryEdge(int diary1Seq, int diary2Seq, double distance) {
            this.diary1Seq = diary1Seq;
            this.diary2Seq = diary2Seq;
            this.distance = distance;
        }

        public int getDiary1Seq() {
            return diary1Seq;
        }

        public int getDiary2Seq() {
            return diary2Seq;
        }

        public double getDistance() {
            return distance;
        }
    }

    /**
     * 내부 클래스: 그룹 간 연결 정보
     */
    private static class GroupEdge {
        private final int group1Id;
        private final int group2Id;
        private final double distance;

        public GroupEdge(int group1Id, int group2Id, double distance) {
            this.group1Id = group1Id;
            this.group2Id = group2Id;
            this.distance = distance;
        }

        public int getGroup1Id() {
            return group1Id;
        }

        public int getGroup2Id() {
            return group2Id;
        }

        public double getDistance() {
            return distance;
        }
    }
}
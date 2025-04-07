package com.c202.diary.util.coordinate.service;

import com.c202.diary.diary.entity.Diary;
import com.c202.diary.tag.entity.DiaryTag;
import com.c202.diary.tag.repository.DiaryTagRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

// 별자리 연결 관계를 생성하고 최적화하는 서비스
// 일기 간의 태그 유사도를 기반으로 최적의 연결 관계를 만들어 별자리처럼 보이도록 합니다.
@Slf4j
@Service
@RequiredArgsConstructor
public class ConstellationConnectionService {

    private final DiaryTagRepository diaryTagRepository;

    // 일기별 최대 연결 수 (시각적으로 복잡해지지 않도록 제한)
    private static final int MAX_CONNECTIONS_PER_DIARY = 2;

    // 최소 연결 유사도 임계값 (이 값 이상의 유사도를 가진 일기만 연결)
    private static final double MIN_SIMILARITY_THRESHOLD = 0.20;

    /**
     * 일기 목록에서 최적의 연결 관계를 생성합니다.
     * @param diaries 일기 목록
     * @return 일기 ID를 키로, 연결된 일기 ID 목록을 값으로 하는 맵
     */
    public Map<Integer, List<Integer>> optimizeConnections(List<Diary> diaries) {
        Map<Integer, List<Integer>> connections = new HashMap<>();

        // 각 일기에 빈 연결 목록 초기화
        for (Diary diary : diaries) {
            connections.put(diary.getDiarySeq(), new ArrayList<>());
        }

        // 일기가 1개 이하면 연결이 필요 없음
        if (diaries.size() <= 1) {
            return connections;
        }

        // 기존 복잡한 연결 대신 단순 연결 구조 적용
        createSimpleConnections(diaries, connections);

        return connections;
    }

    /**
     * 별자리 그룹 내에서 연결 관계를 최적화합니다.
     * @param constellationGroups 별자리 그룹 목록
     * @return 최적화된 연결 관계
     */
    public Map<Integer, List<Integer>> optimizeConstellationConnections(List<List<Diary>> constellationGroups) {
        Map<Integer, List<Integer>> connections = new HashMap<>();

        // 모든 일기 수집 및 초기화
        for (List<Diary> group : constellationGroups) {
            for (Diary diary : group) {
                connections.put(diary.getDiarySeq(), new ArrayList<>());
            }
        }

        // 각 별자리 그룹 내부에 형태 적용
        for (List<Diary> group : constellationGroups) {
            // 각 그룹에 적절한 별자리 형태 연결 적용
            applyConstellationPattern(group, connections);
        }

        // 별자리 그룹 간 최소한의 연결 추가 (필요한 경우)
        if (constellationGroups.size() > 1) {
            connectBetweenConstellations(constellationGroups, connections);
        }

        return connections;
    }

    /**
     * 일기 그룹에 별자리 패턴을 적용합니다.
     * @param diaries 일기 그룹
     * @param connections 연결 관계 맵
     */
    private void applyConstellationPattern(List<Diary> diaries, Map<Integer, List<Integer>> connections) {
        int size = diaries.size();

        if (size <= 1) {
            return; // 일기가 1개 이하면 연결 불필요
        }

        if (size <= 3) {
            // 2-3개 일기는 선형으로 연결
            applyLinearPattern(diaries, connections);
        } else if (size <= 5) {
            // 4-5개 일기는 빌표/W/삼각형 모양으로 연결
            applyStarPattern(diaries, connections);
        } else {
            // 6개 이상은 원형/사이클 모양으로 연결
            applyCircularPattern(diaries, connections);
        }
    }

    /**
     * 선형 패턴 적용 (작은 그룹용)
     */
    private void applyLinearPattern(List<Diary> diaries, Map<Integer, List<Integer>> connections) {
        // 일기들을 순서대로 연결
        for (int i = 0; i < diaries.size() - 1; i++) {
            int current = diaries.get(i).getDiarySeq();
            int next = diaries.get(i + 1).getDiarySeq();

            // 양방향 연결
            connections.get(current).add(next);
            connections.get(next).add(current);
        }
    }

    /**
     * 별/삼각형 패턴 적용 (중간 크기 그룹용)
     */
    private void applyStarPattern(List<Diary> diaries, Map<Integer, List<Integer>> connections) {
        if (diaries.size() <= 3) {
            // 삼각형 패턴
            applyTrianglePattern(diaries, connections);
            return;
        }

        // 중앙 노드 선택 (첫 번째 노드)
        int centerSeq = diaries.get(0).getDiarySeq();

        // 중앙 노드와 나머지 노드들 연결
        for (int i = 1; i < diaries.size(); i++) {
            int nodeSeq = diaries.get(i).getDiarySeq();

            // 양방향 연결
            connections.get(centerSeq).add(nodeSeq);
            connections.get(nodeSeq).add(centerSeq);

            // 연속된 노드 간 추가 연결 (W 모양 등 형성)
            if (i < diaries.size() - 1) {
                int nextSeq = diaries.get(i + 1).getDiarySeq();
                connections.get(nodeSeq).add(nextSeq);
                connections.get(nextSeq).add(nodeSeq);
            }
        }
    }

    /**
     * 삼각형 패턴 적용
     */
    private void applyTrianglePattern(List<Diary> diaries, Map<Integer, List<Integer>> connections) {
        if (diaries.size() < 3) {
            applyLinearPattern(diaries, connections);
            return;
        }

        // 세 점을 모두 연결
        int seq1 = diaries.get(0).getDiarySeq();
        int seq2 = diaries.get(1).getDiarySeq();
        int seq3 = diaries.get(2).getDiarySeq();

        // 양방향 연결
        connections.get(seq1).add(seq2);
        connections.get(seq2).add(seq1);

        connections.get(seq2).add(seq3);
        connections.get(seq3).add(seq2);

        connections.get(seq3).add(seq1);
        connections.get(seq1).add(seq3);
    }

    /**
     * 원형 패턴 적용 (큰 그룹용)
     */
    private void applyCircularPattern(List<Diary> diaries, Map<Integer, List<Integer>> connections) {
        // 일기들을 원형으로 연결 (각 일기는 앞뒤와만 연결)
        for (int i = 0; i < diaries.size(); i++) {
            int current = diaries.get(i).getDiarySeq();
            int next = diaries.get((i + 1) % diaries.size()).getDiarySeq();

            // 양방향 연결
            connections.get(current).add(next);
            connections.get(next).add(current);
        }
    }

    /**
     * 간단한 연결 구조를 만듭니다 (일반 목적용)
     */
    private void createSimpleConnections(List<Diary> diaries, Map<Integer, List<Integer>> connections) {
        if (diaries.size() <= 3) {
            // 2-3개 일기는 모두 직접 연결
            for (int i = 0; i < diaries.size(); i++) {
                for (int j = i + 1; j < diaries.size(); j++) {
                    int seq1 = diaries.get(i).getDiarySeq();
                    int seq2 = diaries.get(j).getDiarySeq();
                    connections.get(seq1).add(seq2);
                    connections.get(seq2).add(seq1);
                }
            }
            return;
        }

        // 각 일기마다 태그 유사도가 가장 높은 1-2개와 연결
        for (int i = 0; i < diaries.size(); i++) {
            Diary current = diaries.get(i);
            List<String> currentTags = getDiaryTags(current);

            // 현재 일기와 다른 일기들 간의 유사도 계산
            List<DiaryConnection> similarities = new ArrayList<>();
            for (int j = 0; j < diaries.size(); j++) {
                if (i == j) continue;

                Diary other = diaries.get(j);
                List<String> otherTags = getDiaryTags(other);
                double similarity = calculateTagSimilarity(currentTags, otherTags);

                if (similarity >= MIN_SIMILARITY_THRESHOLD) {
                    similarities.add(new DiaryConnection(
                            current.getDiarySeq(),
                            other.getDiarySeq(),
                            similarity
                    ));
                }
            }

            // 유사도 순으로 정렬하고 최대 연결 수까지만 선택
            similarities.sort(Comparator.comparing(DiaryConnection::getSimilarity).reversed());
            for (int j = 0; j < Math.min(MAX_CONNECTIONS_PER_DIARY, similarities.size()); j++) {
                DiaryConnection connection = similarities.get(j);
                int seq1 = connection.getDiary1Seq();
                int seq2 = connection.getDiary2Seq();

                // 이미 최대 연결 수에 도달하지 않은 경우만 연결
                if (connections.get(seq2).size() < MAX_CONNECTIONS_PER_DIARY) {
                    // 양방향 연결
                    connections.get(seq1).add(seq2);
                    connections.get(seq2).add(seq1);
                }
            }
        }

        // 모든 일기가 최소 하나의 연결을 가지도록 보장
        ensureMinimalConnections(diaries, connections);
    }

    /**
     * 모든 일기가 최소 하나의 연결을 가지도록 보장합니다.
     */
    private void ensureMinimalConnections(List<Diary> diaries, Map<Integer, List<Integer>> connections) {
        for (Diary diary : diaries) {
            int diarySeq = diary.getDiarySeq();

            // 연결이 없는 일기 찾기
            if (connections.get(diarySeq).isEmpty()) {
                // 가장 가까운(태그 유사도 기준) 다른 일기 찾기
                Diary closestDiary = null;
                double highestSimilarity = 0;

                for (Diary other : diaries) {
                    if (other.getDiarySeq().equals(diarySeq)) continue;

                    double similarity = calculateDiarySimilarity(diary, other);
                    if (similarity > highestSimilarity) {
                        highestSimilarity = similarity;
                        closestDiary = other;
                    }
                }

                // 가장 가까운 일기와 연결
                if (closestDiary != null) {
                    int otherSeq = closestDiary.getDiarySeq();
                    connections.get(diarySeq).add(otherSeq);
                    connections.get(otherSeq).add(diarySeq);
                }
            }
        }
    }

    /**
     * 별자리 그룹 간에 최소한의 연결을 추가합니다.
     */
    private void connectBetweenConstellations(List<List<Diary>> constellationGroups, Map<Integer, List<Integer>> connections) {
        // 각 그룹에서 대표 일기 선택 (간단하게 첫 번째 일기 사용)
        List<Diary> representatives = new ArrayList<>();
        for (List<Diary> group : constellationGroups) {
            if (!group.isEmpty()) {
                representatives.add(group.get(0));
            }
        }

        // 대표 일기들 간에 체인 형태로 연결
        for (int i = 0; i < representatives.size() - 1; i++) {
            int current = representatives.get(i).getDiarySeq();
            int next = representatives.get(i + 1).getDiarySeq();

            connections.get(current).add(next);
            connections.get(next).add(current);
        }
    }

    /**
     * 두 일기 간의 유사도를 계산합니다. (태그 기반)
     */
    private double calculateDiarySimilarity(Diary diary1, Diary diary2) {
        List<String> tags1 = getDiaryTags(diary1);
        List<String> tags2 = getDiaryTags(diary2);

        return calculateTagSimilarity(tags1, tags2);
    }

    /**
     * 일기의 태그 목록을 조회합니다.
     */
    private List<String> getDiaryTags(Diary diary) {
        List<DiaryTag> diaryTags = diaryTagRepository.findByDiary(diary);
        return diaryTags.stream()
                .map(diaryTag -> diaryTag.getTag().getName())
                .collect(Collectors.toList());
    }

    /**
     * 두 태그 목록 간의 자카드 유사도를 계산합니다.
     */
    private double calculateTagSimilarity(List<String> tags1, List<String> tags2) {
        if (tags1.isEmpty() && tags2.isEmpty()) {
            return 0.0;
        }

        if (tags1.isEmpty() || tags2.isEmpty()) {
            return 0.1;
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
    private static class DiaryConnection {
        private final int diary1Seq;
        private final int diary2Seq;
        private double similarity;

        public DiaryConnection(int diary1Seq, int diary2Seq, double similarity) {
            this.diary1Seq = diary1Seq;
            this.diary2Seq = diary2Seq;
            this.similarity = similarity;
        }

        public int getDiary1Seq() {
            return diary1Seq;
        }

        public int getDiary2Seq() {
            return diary2Seq;
        }

        public double getSimilarity() {
            return similarity;
        }

        public void setSimilarity(double similarity) {
            this.similarity = similarity;
        }
    }
}
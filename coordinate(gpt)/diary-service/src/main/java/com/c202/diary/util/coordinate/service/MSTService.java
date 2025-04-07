package com.c202.diary.util.coordinate.service;

import com.c202.diary.diary.entity.Diary;
import com.c202.diary.tag.entity.DiaryTag;
import com.c202.diary.tag.repository.DiaryTagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MSTService {

    private final DiaryTagRepository diaryTagRepository;
    private static final double MAX_CONNECTION_WEIGHT = 100.0;
    private static final int DEFAULT_MAX_CONNECTIONS_PER_DIARY = 3;

    public Map<Integer, List<Integer>> calculateMST(Diary centerDiary, List<Diary> diaries) {
        List<Diary> allDiaries = new ArrayList<>(diaries);
        allDiaries.add(0, centerDiary);
        int n = allDiaries.size();

        double[][] distMatrix = new double[n][n];
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < n; j++) {
                if (i == j) {
                    distMatrix[i][j] = Double.MAX_VALUE;
                } else {
                    double similarity = calculateTagSimilarity(
                            getDiaryTags(allDiaries.get(i)),
                            getDiaryTags(allDiaries.get(j))
                    );
                    distMatrix[i][j] = similarity > 0 ? 1.0 / similarity : MAX_CONNECTION_WEIGHT;
                }
            }
        }

        boolean[] inMST = new boolean[n];
        double[] key = new double[n];
        int[] parent = new int[n];
        Arrays.fill(key, Double.MAX_VALUE);
        Arrays.fill(parent, -1);
        key[0] = 0;

        for (int count = 0; count < n; count++) {
            int u = -1;
            double minKey = Double.MAX_VALUE;
            for (int i = 0; i < n; i++) {
                if (!inMST[i] && key[i] < minKey) {
                    minKey = key[i];
                    u = i;
                }
            }
            if (u == -1) break;
            inMST[u] = true;
            for (int v = 0; v < n; v++) {
                if (!inMST[v] && distMatrix[u][v] < key[v]) {
                    parent[v] = u;
                    key[v] = distMatrix[u][v];
                }
            }
        }

        Map<Integer, List<Integer>> connections = new HashMap<>();
        for (int i = 0; i < n; i++) {
            connections.put(allDiaries.get(i).getDiarySeq(), new ArrayList<>());
        }
        for (int i = 1; i < n; i++) {
            if (parent[i] != -1) {
                int diary1Seq = allDiaries.get(parent[i]).getDiarySeq();
                int diary2Seq = allDiaries.get(i).getDiarySeq();
                connections.get(diary1Seq).add(diary2Seq);
                connections.get(diary2Seq).add(diary1Seq);
            }
        }

        return limitConnectionsInMST(connections, centerDiary, diaries, DEFAULT_MAX_CONNECTIONS_PER_DIARY);
    }

    private Map<Integer, List<Integer>> limitConnectionsInMST(Map<Integer, List<Integer>> mstConnections,
                                                              Diary centerDiary,
                                                              List<Diary> diaries,
                                                              int maxConnectionsPerDiary) {
        Map<Integer, List<Integer>> limitedConnections = new HashMap<>();
        List<Diary> allDiaries = new ArrayList<>(diaries);
        allDiaries.add(0, centerDiary);
        for (Diary diary : allDiaries) {
            Integer diarySeq = diary.getDiarySeq();
            List<Integer> connections = mstConnections.getOrDefault(diarySeq, new ArrayList<>());
            if (connections.size() <= maxConnectionsPerDiary) {
                limitedConnections.put(diarySeq, new ArrayList<>(connections));
                continue;
            }
            Map<Integer, Double> similarityScores = new HashMap<>();
            for (Integer connectedDiarySeq : connections) {
                Diary connectedDiary = allDiaries.stream()
                        .filter(d -> d.getDiarySeq().equals(connectedDiarySeq))
                        .findFirst()
                        .orElse(null);
                if (connectedDiary != null) {
                    double similarity = calculateTagSimilarity(
                            getDiaryTags(diary),
                            getDiaryTags(connectedDiary)
                    );
                    similarityScores.put(connectedDiarySeq, similarity);
                }
            }
            List<Integer> limitedConnectionsList = similarityScores.entrySet().stream()
                    .sorted(Map.Entry.<Integer, Double>comparingByValue().reversed())
                    .limit(maxConnectionsPerDiary)
                    .map(Map.Entry::getKey)
                    .collect(Collectors.toList());
            limitedConnections.put(diarySeq, limitedConnectionsList);
        }
        return limitedConnections;
    }

    private List<String> getDiaryTags(Diary diary) {
        List<DiaryTag> diaryTags = diaryTagRepository.findByDiary(diary);
        return diaryTags.stream()
                .map(diaryTag -> diaryTag.getTag().getName())
                .collect(Collectors.toList());
    }

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
}

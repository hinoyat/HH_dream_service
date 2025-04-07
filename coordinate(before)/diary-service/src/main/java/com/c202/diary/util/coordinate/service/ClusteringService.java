package com.c202.diary.util.coordinate.service;

import com.c202.diary.emotion.entity.Emotion;
import com.c202.diary.tag.entity.DiaryTag;
import com.c202.diary.tag.repository.DiaryTagRepository;
import com.c202.diary.util.coordinate.model.ClusterDto;
import com.c202.diary.diary.entity.Diary;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClusteringService {

    private final DiaryTagRepository diaryTagRepository;
    private static final double CLUSTER_SIMILARITY_THRESHOLD = 0.3;

    public List<ClusterDto> formClusters(List<Diary> diaries, List<String> newDiaryTags) {
        if (diaries.isEmpty()) {
            return new ArrayList<>();
        }
        List<ClusterDto> clusters = new ArrayList<>();
        Set<Integer> assignedDiaries = new HashSet<>();

        for (Diary diary : diaries) {
            if (assignedDiaries.contains(diary.getDiarySeq())) {
                continue;
            }
            ClusterDto cluster = new ClusterDto();
            cluster.getDiaries().add(diary);

            List<String> diaryTags = getDiaryTags(diary);

            for (Diary otherDiary : diaries) {
                if (otherDiary.getDiarySeq().equals(diary.getDiarySeq()) ||
                        assignedDiaries.contains(otherDiary.getDiarySeq())) {
                    continue;
                }
                List<String> otherTags = getDiaryTags(otherDiary);
                double similarity = calculateTagSimilarity(diaryTags, otherTags);
                if (similarity >= CLUSTER_SIMILARITY_THRESHOLD) {
                    cluster.getDiaries().add(otherDiary);
                    assignedDiaries.add(otherDiary.getDiarySeq());
                }
            }

            calculateClusterCenter(cluster);

            // 새 일기와의 유사도 계산 (클러스터의 전체 태그 집합으로)
            cluster.setSimilarityToNewDiary(
                    calculateTagSimilarity(
                            cluster.getDiaries().stream()
                                    .flatMap(d -> getDiaryTags(d).stream())
                                    .distinct()
                                    .collect(Collectors.toList()),
                            newDiaryTags
                    )
            );

            clusters.add(cluster);
            assignedDiaries.add(diary.getDiarySeq());
        }
        return clusters;
    }

    public void calculateClusterCenter(ClusterDto cluster) {
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

    public ClusterDto createNewCluster(Emotion emotion) {
        ClusterDto cluster = new ClusterDto();
        Random random = new Random();
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

    public ClusterDto findBestCluster(List<ClusterDto> clusters, List<String> tags) {
        return clusters.stream()
                .max(Comparator.comparing(ClusterDto::getSimilarityToNewDiary))
                .orElse(clusters.get(0));
    }

    // 공개 메서드로 노트의 태그와 유사도 계산 기능 제공
    public List<String> getDiaryTags(Diary diary) {
        List<DiaryTag> diaryTags = diaryTagRepository.findByDiary(diary);
        return diaryTags.stream()
                .map(diaryTag -> diaryTag.getTag().getName())
                .collect(Collectors.toList());
    }

    public double calculateTagSimilarity(List<String> tags1, List<String> tags2) {
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

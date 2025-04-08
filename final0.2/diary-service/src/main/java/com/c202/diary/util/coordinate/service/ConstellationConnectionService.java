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
    private static final double MAX_CONNECTION_DISTANCE = 50.0;

    // 구의 반경 (전체 우주의 크기)
    private static final double SPHERE_RADIUS = 100.0;

    // 별자리 패턴 유형 열거
    // ConstellationLayoutService와 동일한 열거형 사용
    private enum PatternType {
        // 실제 별자리
        ORION, BIG_DIPPER, CASSIOPEIA, GEMINI, LIBRA, SOUTHERN_CROSS, CORONA, PERSEUS, LYRA, SAGITTARIUS,
        // 기하학적 패턴
        STAR, CIRCLE, TRIANGLE, ARROW, ZIGZAG, WAVE, HEXAGON, SPIRAL, CROSS, SCATTER
    }

    // 오리온자리 연결 패턴 (인덱스 쌍 배열)
    private static final int[][] ORION_CONNECTIONS = {
            {0, 1}, {0, 3}, {1, 2}, {2, 3}, {3, 4}, {3, 5}, {4, 6}
    };

    // 북두칠성(큰곰자리) 연결 패턴
    private static final int[][] BIG_DIPPER_CONNECTIONS = {
            {0, 1}, {1, 2}, {2, 3}, {3, 4}, {4, 5}, {5, 6}
    };

    // 카시오페이아자리 연결 패턴
    private static final int[][] CASSIOPEIA_CONNECTIONS = {
            {0, 1}, {1, 2}, {2, 3}, {3, 4}, {4, 5}, {5, 6}
    };

    // 쌍둥이자리 연결 패턴
    private static final int[][] GEMINI_CONNECTIONS = {
            {0, 1}, {1, 2}, {3, 4}, {4, 5}, {2, 6}, {5, 6}
    };

    // 천칭자리 연결 패턴
    private static final int[][] LIBRA_CONNECTIONS = {
            {0, 2}, {1, 2}, {2, 3}, {2, 4}, {4, 5}
    };

    // 남십자자리 연결 패턴
    private static final int[][] SOUTHERN_CROSS_CONNECTIONS = {
            {0, 2}, {1, 2}, {2, 3}, {2, 4}
    };

    // 왕관자리 연결 패턴
    private static final int[][] CORONA_CONNECTIONS = {
            {0, 1}, {1, 2}, {2, 3}, {3, 4}, {5, 1}, {5, 3}, {5, 6}
    };

    // 페르세우스자리 연결 패턴
    private static final int[][] PERSEUS_CONNECTIONS = {
            {0, 1}, {0, 2}, {1, 3}, {2, 3}, {3, 4}, {3, 5}, {4, 6}
    };

    // 거문고자리 연결 패턴
    private static final int[][] LYRA_CONNECTIONS = {
            {0, 1}, {0, 2}, {1, 3}, {2, 4}, {3, 5}, {4, 5}
    };

    // 궁수자리 연결 패턴
    private static final int[][] SAGITTARIUS_CONNECTIONS = {
            {0, 1}, {0, 2}, {0, 3}, {3, 4}, {3, 5}, {3, 6}
    };

    // 별 모양 연결 패턴
    private static final int[][] STAR_CONNECTIONS = {
            {0, 1}, {0, 2}, {0, 3}, {0, 4}, {0, 5}, {0, 6}
    };

    // 원형 연결 패턴
    private static final int[][] CIRCLE_CONNECTIONS = {
            {0, 1}, {1, 2}, {2, 3}, {3, 4}, {4, 5}, {5, 6}, {6, 0}
    };

    // 삼각형 연결 패턴
    private static final int[][] TRIANGLE_CONNECTIONS = {
            {0, 1}, {1, 2}, {2, 0}, {3, 0}, {3, 1}, {3, 2}, {4, 5}
    };

    // 화살표 연결 패턴
    private static final int[][] ARROW_CONNECTIONS = {
            {0, 1}, {0, 2}, {0, 3}, {3, 4}, {4, 5}, {4, 6}
    };

    // 지그재그 연결 패턴
    private static final int[][] ZIGZAG_CONNECTIONS = {
            {0, 1}, {1, 2}, {2, 3}, {3, 4}, {4, 5}, {5, 6}
    };

    // 파도 연결 패턴
    private static final int[][] WAVE_CONNECTIONS = {
            {0, 1}, {1, 2}, {2, 3}, {3, 4}, {4, 5}, {5, 6}
    };

    // 육각형 연결 패턴
    private static final int[][] HEXAGON_CONNECTIONS = {
            {0, 1}, {1, 2}, {2, 3}, {3, 4}, {4, 5}, {5, 0}, {6, 0}, {6, 3}
    };

    // 나선형 연결 패턴
    private static final int[][] SPIRAL_CONNECTIONS = {
            {0, 1}, {1, 2}, {2, 3}, {3, 4}, {4, 5}, {5, 6}
    };

    // 십자가 연결 패턴
    private static final int[][] CROSS_CONNECTIONS = {
            {0, 2}, {1, 2}, {2, 3}, {2, 4}, {2, 5}, {2, 6}
    };

    // 흩어진 형태 연결 패턴 (중심점에서 방사형으로 연결)
    private static final int[][] SCATTER_CONNECTIONS = {
            {0, 1}, {0, 2}, {0, 3}, {0, 4}, {0, 5}, {0, 6}
    };

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

        // 별자리 패턴을 기준으로 연결 구조 결정
        PatternType patternType = determinePatternType(diaries);

        // 선택된 패턴 유형에 따른 연결 구조 가져오기
        int[][] connectionPattern = getConnectionPatternByType(patternType, size);

        // 패턴에 따른 연결 적용
        for (int[] connection : connectionPattern) {
            // 인덱스가 유효한지 확인
            if (connection[0] < size && connection[1] < size) {
                Diary diary1 = diaries.get(connection[0]);
                Diary diary2 = diaries.get(connection[1]);

                // 거리 확인 (너무 멀리 떨어진 일기는 연결하지 않음)
                if (calculateDistance(diary1, diary2) <= MAX_CONNECTION_DISTANCE) {
                    addConnection(connections, diary1.getDiarySeq(), diary2.getDiarySeq());
                }
            }
        }

        // 추가적인 연결 생성 (별자리 모양에 자연스러움 추가, 필요한 경우)
        if (patternType == PatternType.SCATTER) {
            enhanceScatterConnections(diaries, connections);
        }
    }

    /**
     * 일기 그룹에서 별자리 패턴 유형을 결정합니다.
     * 좌표 분석을 통해 어떤 패턴이 적용되었는지 추론합니다.
     */
    private PatternType determinePatternType(List<Diary> diaries) {
        // 기본값 설정 (나중에 좌표 분석을 통해 개선할 수 있음)
        int count = diaries.size();

        // 일기 수를 기반으로 가장 적합한 패턴 선택
        if (count == 7) {
            return PatternType.BIG_DIPPER; // 7개이면 북두칠성 형태
        } else if (count == 5) {
            return PatternType.CASSIOPEIA; // 5개이면 카시오페이아자리 형태
        } else if (count == 4) {
            return PatternType.SOUTHERN_CROSS; // 4개이면 남십자자리 형태
        } else if (count == 3) {
            return PatternType.TRIANGLE; // 3개이면 삼각형 형태
        } else if (count == 6) {
            return PatternType.HEXAGON; // 6개이면 육각형 형태
        } else {
            // 기타 경우: 좌표 분석 기반 결정 (여기서는 간단히 처리)
            return PatternType.STAR; // 기본값
        }

        // 참고: 실제로는 좌표 분석을 통해 더 정확한 패턴 추론 가능
        // 예: 원형 배치인지, 별 모양인지 등을 좌표 분포로 판단
    }

    /**
     * 패턴 유형에 따른 연결 구조를 반환합니다.
     */
    private int[][] getConnectionPatternByType(PatternType type, int count) {
        switch (type) {
            case ORION:          return limitConnectionsByCount(ORION_CONNECTIONS, count);
            case BIG_DIPPER:     return limitConnectionsByCount(BIG_DIPPER_CONNECTIONS, count);
            case CASSIOPEIA:     return limitConnectionsByCount(CASSIOPEIA_CONNECTIONS, count);
            case GEMINI:         return limitConnectionsByCount(GEMINI_CONNECTIONS, count);
            case LIBRA:          return limitConnectionsByCount(LIBRA_CONNECTIONS, count);
            case SOUTHERN_CROSS: return limitConnectionsByCount(SOUTHERN_CROSS_CONNECTIONS, count);
            case CORONA:         return limitConnectionsByCount(CORONA_CONNECTIONS, count);
            case PERSEUS:        return limitConnectionsByCount(PERSEUS_CONNECTIONS, count);
            case LYRA:           return limitConnectionsByCount(LYRA_CONNECTIONS, count);
            case SAGITTARIUS:    return limitConnectionsByCount(SAGITTARIUS_CONNECTIONS, count);
            case STAR:           return limitConnectionsByCount(STAR_CONNECTIONS, count);
            case CIRCLE:         return limitConnectionsByCount(CIRCLE_CONNECTIONS, count);
            case TRIANGLE:       return limitConnectionsByCount(TRIANGLE_CONNECTIONS, count);
            case ARROW:          return limitConnectionsByCount(ARROW_CONNECTIONS, count);
            case ZIGZAG:         return limitConnectionsByCount(ZIGZAG_CONNECTIONS, count);
            case WAVE:           return limitConnectionsByCount(WAVE_CONNECTIONS, count);
            case HEXAGON:        return limitConnectionsByCount(HEXAGON_CONNECTIONS, count);
            case SPIRAL:         return limitConnectionsByCount(SPIRAL_CONNECTIONS, count);
            case CROSS:          return limitConnectionsByCount(CROSS_CONNECTIONS, count);
            case SCATTER:        return limitConnectionsByCount(SCATTER_CONNECTIONS, count);
            default:             return limitConnectionsByCount(STAR_CONNECTIONS, count);
        }
    }

    /**
     * 연결 패턴을 일기 수에 맞게 제한합니다.
     */
    private int[][] limitConnectionsByCount(int[][] connections, int count) {
        List<int[]> validConnections = new ArrayList<>();

        for (int[] connection : connections) {
            if (connection[0] < count && connection[1] < count) {
                validConnections.add(connection);
            }
        }

        return validConnections.toArray(new int[0][]);
    }

    /**
     * 흩어진 형태의 연결을 강화합니다.
     */
    private void enhanceScatterConnections(List<Diary> diaries, Map<Integer, List<Integer>> connections) {
        if (diaries.size() <= 3) return;

        // 중심점 (일반적으로 인덱스 0)
        Diary centerDiary = diaries.get(0);
        int centerSeq = centerDiary.getDiarySeq();

        // 각 일기와 다른 일기 간의 태그 유사도 계산
        for (int i = 1; i < diaries.size(); i++) {
            Diary diary1 = diaries.get(i);
            int diary1Seq = diary1.getDiarySeq();

            // 이미 중심과 연결되어 있는지 확인
            boolean connectedToCenter = connections.get(diary1Seq).contains(centerSeq);

            // 다른 일기들과 유사도 확인
            for (int j = i + 1; j < diaries.size(); j++) {
                Diary diary2 = diaries.get(j);
                int diary2Seq = diary2.getDiarySeq();

                // 태그 유사도 계산
                List<String> tags1 = getDiaryTags(diary1);
                List<String> tags2 = getDiaryTags(diary2);
                double similarity = calculateTagSimilarity(tags1, tags2);

                // 거리 계산
                double distance = calculateDistance(diary1, diary2);

                // 유사도가 높고 거리가 가까우면 연결
                if (similarity > 0.3 && distance <= MAX_CONNECTION_DISTANCE * 0.7 &&
                        connections.get(diary1Seq).size() < MAX_CONNECTIONS_PER_DIARY &&
                        connections.get(diary2Seq).size() < MAX_CONNECTIONS_PER_DIARY) {

                    addConnection(connections, diary1Seq, diary2Seq);
                }
            }
        }
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

        // 각 그룹쌍마다 가장 가까운 일기 쌍을 찾아 연결
        for (int i = 0; i < groupIds.size(); i++) {
            for (int j = i + 1; j < groupIds.size(); j++) {
                connectClosestDiaries(
                        findGroupById(constellationGroups, groupIds.get(i)),
                        findGroupById(constellationGroups, groupIds.get(j)),
                        connections
                );
            }
        }
    }

    /**
     * 그룹 ID로 그룹 찾기
     */
    private List<Diary> findGroupById(List<List<Diary>> groups, int groupId) {
        for (List<Diary> group : groups) {
            if (!group.isEmpty() && group.get(0).getDiarySeq() == groupId) {
                return group;
            }
        }
        return Collections.emptyList();
    }

    /**
     * 두 그룹에서 가장 가까운 일기 쌍을 찾아 연결
     */
    private void connectClosestDiaries(List<Diary> group1, List<Diary> group2, Map<Integer, List<Integer>> connections) {
        if (group1.isEmpty() || group2.isEmpty()) return;

        Diary closest1 = null;
        Diary closest2 = null;
        double minDistance = Double.MAX_VALUE;

        for (Diary d1 : group1) {
            for (Diary d2 : group2) {
                double dist = calculateDistance(d1, d2);

                // 최대 거리 제한 및 각 일기의 최대 연결 수 확인
                if (dist < minDistance && dist <= MAX_CONNECTION_DISTANCE &&
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
            log.debug("그룹 간 연결 추가: {} - {}", closest1.getDiarySeq(), closest2.getDiarySeq());
        }
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
}
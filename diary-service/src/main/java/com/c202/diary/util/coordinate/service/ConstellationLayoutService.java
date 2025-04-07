package com.c202.diary.util.coordinate.service;

import com.c202.diary.diary.entity.Diary;
import com.c202.diary.emotion.entity.Emotion;
import com.c202.diary.emotion.repository.EmotionRepository;
import com.c202.exception.types.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * 별자리 형태의 일기 좌표 배치를 담당하는 서비스
 * 다양한 별자리 패턴 템플릿을 활용하여 3D 공간에 일기를 배치합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class ConstellationLayoutService {

    private final EmotionRepository emotionRepository;

    // 별자리 템플릿의 기본 스케일 (감정 영역 내부에 맞게 조정됨)
    private static final double BASE_SCALE = 30.0;

    // 별자리 간 최소 거리
    private static final double MIN_CONSTELLATION_DISTANCE = 15.0;

    // 클러스터별 최대 일기 수 (이보다 많으면 여러 별자리로 분할)
    private static final int MAX_DIARIES_PER_CONSTELLATION = 7;

    // 클러스터별 최소 일기 수 (이보다 적으면 특별한 패턴 적용)
    private static final int MIN_DIARIES_PER_CONSTELLATION = 3;

    /**
     * 감정에 맞는 별자리 좌표 템플릿을 생성합니다.
     * @param emotion 감정 엔티티
     * @param diaries 배치할 일기 목록
     * @return 생성된 좌표 배열 (각 일기별 [x, y, z] 좌표)
     */
    public double[][] generateConstellationLayout(Emotion emotion, List<Diary> diaries) {
        int count = diaries.size();

        // 일기 수에 따라 적절한 템플릿 선택
        double[][] templatePositions = selectConstellationTemplate(emotion.getName(), count);

        // 템플릿 좌표를 감정 영역에 맞게 스케일링 및 위치 조정
        return adjustTemplateToCenterAndRadius(
                templatePositions,
                emotion.getBaseX(),
                emotion.getBaseY(),
                emotion.getBaseZ(),
                emotion.getBaseRadius() * 0.8  // 감정 영역 반경의 80%까지만 사용
        );
    }

    /**
     * 여러 그룹의 일기들에 대한 별자리 좌표를 생성합니다.
     * 감정 영역 내에 여러 개의 별자리를 배치합니다.
     * @param emotion 감정 엔티티
     * @param constellationGroups 별자리 그룹 목록 (각 그룹은 하나의 별자리를 형성)
     * @return 그룹별 좌표 배열 맵
     */
    public Map<Integer, double[][]> generateMultipleConstellations(
            Emotion emotion,
            List<List<Diary>> constellationGroups) {

        Map<Integer, double[][]> constellationCoordinates = new HashMap<>();
        int groupCount = constellationGroups.size();

        // 감정 영역 내에 그룹 수만큼 중심점 배치
        double[][] centerPoints = distributePointsInSphere(
                emotion.getBaseX(),
                emotion.getBaseY(),
                emotion.getBaseZ(),
                emotion.getBaseRadius() * 0.7,  // 영역 반경의 70%까지 사용
                groupCount
        );

        // 각 그룹별로 별자리 좌표 생성
        for (int i = 0; i < groupCount; i++) {
            List<Diary> group = constellationGroups.get(i);
            double[] centerPoint = centerPoints[i];

            // 개별 별자리의 크기는 그룹 크기에 비례하되 일정 범위 내로 제한
            double constellationScale = Math.min(
                    emotion.getBaseRadius() * 0.3,  // 최대 크기
                    BASE_SCALE * Math.log10(group.size() + 1)  // 로그 스케일로 크기 조정
            );

            // 별자리 템플릿 선택 및 좌표 생성
            double[][] templatePositions = selectConstellationTemplate(emotion.getName(), group.size());
            double[][] adjustedPositions = adjustTemplateToCenterAndRadius(
                    templatePositions,
                    centerPoint[0],
                    centerPoint[1],
                    centerPoint[2],
                    constellationScale
            );

            // 그룹 내 첫 번째 일기의 ID를 키로 사용
            int groupKey = group.get(0).getDiarySeq();
            constellationCoordinates.put(groupKey, adjustedPositions);
        }

        return constellationCoordinates;
    }

    /**
     * 감정 이름과 일기 수에 따라 적절한 별자리 템플릿을 선택합니다.
     * @param emotionName 감정 이름
     * @param count 일기 수
     * @return 선택된 템플릿 좌표 배열
     */
    private double[][] selectConstellationTemplate(String emotionName, int count) {
        // 일기 수가 정해진 범위를 벗어나면 조정
        count = Math.min(Math.max(count, MIN_DIARIES_PER_CONSTELLATION), MAX_DIARIES_PER_CONSTELLATION);

        // 감정별로 특색 있는 템플릿 선택
        switch (emotionName) {
            case "행복":
                return count <= 5 ? pentagonTemplate(count) : crownTemplate(count);
            case "슬픔":
                return count <= 4 ? tearsTemplate(count) : riverTemplate(count);
            case "분노":
                return count <= 5 ? spikeTemplate(count) : explosionTemplate(count);
            case "불안":
                return count <= 4 ? zigzagTemplate(count) : spiralTemplate(count);
            case "평화":
                return count <= 5 ? circleTemplate(count) : balanceTemplate(count);
            case "희망":
                return count <= 4 ? arrowTemplate(count) : riseTemplate(count);
            case "공포":
                return count <= 5 ? scatterTemplate(count) : chaosTemplate(count);
            default:
                // 기본값으로 삼각형이나 원형 사용
                return count <= 4 ? triangleTemplate(count) : circleTemplate(count);
        }
    }

    /**
     * 템플릿 좌표를 중심점과 반경에 맞게 조정합니다.
     * @param template 원본 템플릿 좌표
     * @param centerX 중심 X 좌표
     * @param centerY 중심 Y 좌표
     * @param centerZ 중심 Z 좌표
     * @param radius 반경
     * @return 조정된 좌표 배열
     */
    private double[][] adjustTemplateToCenterAndRadius(
            double[][] template,
            double centerX,
            double centerY,
            double centerZ,
            double radius) {

        int count = template.length;
        double[][] adjusted = new double[count][3];

        // 템플릿의 최대 거리 계산 (정규화를 위해)
        double maxDistance = 0;
        for (double[] point : template) {
            double distance = Math.sqrt(point[0] * point[0] + point[1] * point[1] + point[2] * point[2]);
            maxDistance = Math.max(maxDistance, distance);
        }

        // 템플릿을 지정된 반경 내로 스케일링하고 중심으로 이동
        for (int i = 0; i < count; i++) {
            double scale = radius / Math.max(maxDistance, 1);
            adjusted[i][0] = centerX + template[i][0] * scale;
            adjusted[i][1] = centerY + template[i][1] * scale;
            adjusted[i][2] = centerZ + template[i][2] * scale;
        }

        return adjusted;
    }

    /**
     * 구 내부에 균등하게 분포된 점들을 생성합니다.
     * 별자리 중심점 배치에 사용됩니다.
     * @param centerX 구의 중심 X 좌표
     * @param centerY 구의 중심 Y 좌표
     * @param centerZ 구의 중심 Z 좌표
     * @param radius 구의 반경
     * @param count 생성할 점의 개수
     * @return 생성된 점들의 좌표 배열
     */
    private double[][] distributePointsInSphere(
            double centerX,
            double centerY,
            double centerZ,
            double radius,
            int count) {

        if (count == 1) {
            // 한 개만 필요하면 중앙에 배치
            return new double[][]{{centerX, centerY, centerZ}};
        }

        double[][] points = new double[count][3];
        Random random = new Random();

        // 피보나치 나선 알고리즘으로 구 표면에 균등하게 점 분포
        double goldenRatio = (1 + Math.sqrt(5)) / 2;

        for (int i = 0; i < count; i++) {
            double t = (double) i / count;
            double inclination = Math.acos(1 - 2 * t);
            double azimuth = 2 * Math.PI * i / goldenRatio;

            // 구면 좌표를 3D 직교 좌표로 변환
            double x = Math.sin(inclination) * Math.cos(azimuth);
            double y = Math.sin(inclination) * Math.sin(azimuth);
            double z = Math.cos(inclination);

            // 반경 내 랜덤한 깊이로 조정 (표면이 아닌 내부에도 배치)
            double depth = 0.3 + 0.7 * random.nextDouble(); // 30%~100% 깊이

            points[i][0] = centerX + radius * x * depth;
            points[i][1] = centerY + radius * y * depth;
            points[i][2] = centerZ + radius * z * depth;
        }

        return points;
    }

    /**
     * 감정 이름으로 감정 엔티티를 조회합니다.
     * @param emotionName 감정 이름
     * @return 감정 엔티티
     */
    public Emotion getEmotionByName(String emotionName) {
        return emotionRepository.findByName(emotionName)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 감정입니다: " + emotionName));
    }

    /**
     * 감정 시퀀스로 감정 엔티티를 조회합니다.
     * @param emotionSeq 감정 시퀀스
     * @return 감정 엔티티
     */
    public Emotion getEmotionBySeq(Integer emotionSeq) {
        return emotionRepository.findById(emotionSeq)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 감정입니다 (seq: " + emotionSeq + ")"));
    }

    /*
     * 여기부터는 다양한 별자리 템플릿 정의
     * 각 템플릿은 원점(0,0,0)을 중심으로 정규화된 좌표 반환
     * 이후 실제 위치와 크기에 맞게 변환됨
     */

    // 1. 삼각형 템플릿 (3-4개 일기)
    private double[][] triangleTemplate(int count) {
        double[][] template = new double[count][3];

        // 기본 삼각형 위치
        template[0] = new double[]{0, 0, 0};       // 중심
        template[1] = new double[]{1, 0, 0};       // 우측
        template[2] = new double[]{-0.5, 0.866, 0}; // 좌측 상단

        if (count >= 4) {
            template[3] = new double[]{-0.5, -0.866, 0}; // 좌측 하단
        }

        return template;
    }

    // 2. 원형 템플릿 (5-7개 일기)
    private double[][] circleTemplate(int count) {
        double[][] template = new double[count][3];

        // 중심점
        template[0] = new double[]{0, 0, 0};

        // 원형으로 배치
        for (int i = 1; i < count; i++) {
            double angle = 2 * Math.PI * (i - 1) / (count - 1);
            template[i] = new double[]{
                    Math.cos(angle),
                    Math.sin(angle),
                    0
            };
        }

        return template;
    }

    // 3. 오각형 템플릿 (행복 감정용, 5개 이하)
    private double[][] pentagonTemplate(int count) {
        double[][] template = new double[count][3];

        // 중심점
        template[0] = new double[]{0, 0, 0};

        // 나머지 점들을 정오각형 꼭지점에 배치
        int placed = 1;
        for (int i = 0; i < 5 && placed < count; i++) {
            double angle = 2 * Math.PI * i / 5;
            template[placed++] = new double[]{
                    Math.cos(angle),
                    Math.sin(angle),
                    0
            };
        }

        return template;
    }

    // 4. 왕관 템플릿 (행복 감정용, 6-7개)
    private double[][] crownTemplate(int count) {
        double[][] template = new double[count][3];

        // 중심점
        template[0] = new double[]{0, 0, 0};

        // 아래 라인 (반원)
        for (int i = 1; i <= 3; i++) {
            double angle = Math.PI * (i - 1) / 2;
            template[i] = new double[]{
                    Math.cos(angle),
                    -0.5,
                    Math.sin(angle) * 0.3
            };
        }

        // 위쪽 포인트 (왕관 모양)
        for (int i = 4; i < count; i++) {
            int idx = i - 4;
            double angle = Math.PI * idx / (count - 4);
            template[i] = new double[]{
                    Math.cos(angle) * 0.8,
                    0.8,
                    Math.sin(angle) * 0.2
            };
        }

        return template;
    }

    // 5. 눈물 템플릿 (슬픔 감정용, 4개 이하)
    private double[][] tearsTemplate(int count) {
        double[][] template = new double[count][3];

        // 기본 눈물 모양
        template[0] = new double[]{0, 0, 0};      // 상단
        template[1] = new double[]{-0.3, -0.5, 0}; // 좌측

        if (count >= 3) {
            template[2] = new double[]{0.3, -0.5, 0}; // 우측
        }

        if (count >= 4) {
            template[3] = new double[]{0, -1, 0};    // 하단
        }

        return template;
    }

    // 6. 강 템플릿 (슬픔 감정용, 5-7개)
    private double[][] riverTemplate(int count) {
        double[][] template = new double[count][3];

        // 곡선 형태의 강 모양
        for (int i = 0; i < count; i++) {
            double t = (double) i / (count - 1);
            // 사인 곡선으로 흐르는 강 형태
            template[i] = new double[]{
                    t * 2 - 1,
                    Math.sin(t * Math.PI) * 0.5,
                    Math.cos(t * Math.PI * 2) * 0.2
            };
        }

        return template;
    }

    // 7. 뾰족한 템플릿 (분노 감정용, 5개 이하)
    private double[][] spikeTemplate(int count) {
        double[][] template = new double[count][3];

        // 중앙점
        template[0] = new double[]{0, 0, 0};

        // 방사형으로 뻗은 뾰족한 점들
        for (int i = 1; i < count; i++) {
            double angle = 2 * Math.PI * (i - 1) / (count - 1);
            double length = 0.8 + 0.4 * Math.random(); // 불규칙한 길이
            template[i] = new double[]{
                    Math.cos(angle) * length,
                    Math.sin(angle) * length,
                    (Math.random() - 0.5) * 0.3 // Z축 변화
            };
        }

        return template;
    }

    // 8. 폭발 템플릿 (분노 감정용, 6-7개)
    private double[][] explosionTemplate(int count) {
        double[][] template = new double[count][3];

        // 중앙점
        template[0] = new double[]{0, 0, 0};

        // 불규칙한 폭발 형태
        for (int i = 1; i < count; i++) {
            double phi = Math.acos(2 * Math.random() - 1); // 균등 분포 각도
            double theta = 2 * Math.PI * Math.random();
            double r = 0.5 + 0.5 * Math.random(); // 다양한 거리

            template[i] = new double[]{
                    r * Math.sin(phi) * Math.cos(theta),
                    r * Math.sin(phi) * Math.sin(theta),
                    r * Math.cos(phi)
            };
        }

        return template;
    }

    // 9. 지그재그 템플릿 (불안 감정용, 4개 이하)
    private double[][] zigzagTemplate(int count) {
        double[][] template = new double[count][3];

        // 지그재그 패턴
        for (int i = 0; i < count; i++) {
            double t = (double) i / (count - 1);
            template[i] = new double[]{
                    t * 2 - 1,
                    (i % 2 == 0) ? 0.5 : -0.5,
                    0
            };
        }

        return template;
    }

    // 10. 나선형 템플릿 (불안 감정용, 5-7개)
    private double[][] spiralTemplate(int count) {
        double[][] template = new double[count][3];

        // 나선형 패턴
        for (int i = 0; i < count; i++) {
            double t = (double) i / (count - 1);
            double radius = t * 0.8;
            double angle = t * 4 * Math.PI;

            template[i] = new double[]{
                    radius * Math.cos(angle),
                    radius * Math.sin(angle),
                    t * 0.5 - 0.25
            };
        }

        return template;
    }

    // 11. 화살표 템플릿 (희망 감정용, 4개 이하)
    private double[][] arrowTemplate(int count) {
        double[][] template = new double[count][3];

        // 화살표 형태
        template[0] = new double[]{0, 1, 0};   // 화살촉
        template[1] = new double[]{0, 0, 0};   // 중앙

        if (count >= 3) {
            template[2] = new double[]{-0.5, 0.5, 0}; // 좌측 날개
        }

        if (count >= 4) {
            template[3] = new double[]{0.5, 0.5, 0};  // 우측 날개
        }

        return template;
    }

    // 12. 상승 템플릿 (희망 감정용, 5-7개)
    private double[][] riseTemplate(int count) {
        double[][] template = new double[count][3];

        // 상승 곡선
        for (int i = 0; i < count; i++) {
            double t = (double) i / (count - 1);
            template[i] = new double[]{
                    t * 2 - 1,
                    t * t,
                    (i % 2 == 0) ? 0.2 : -0.2
            };
        }

        return template;
    }

    // 13. 균형 템플릿 (평화 감정용, 6-7개)
    private double[][] balanceTemplate(int count) {
        double[][] template = new double[count][3];

        // 균형 있는 형태 (음양)
        template[0] = new double[]{0, 0, 0}; // 중앙

        int half = (count - 1) / 2;

        // 윗부분 곡선
        for (int i = 1; i <= half; i++) {
            double t = (double) (i - 1) / half;
            template[i] = new double[]{
                    Math.cos(t * Math.PI) * 0.5,
                    0.5,
                    Math.sin(t * Math.PI) * 0.3
            };
        }

        // 아랫부분 곡선
        for (int i = half + 1; i < count; i++) {
            double t = (double) (i - half - 1) / (count - half - 1);
            template[i] = new double[]{
                    -Math.cos(t * Math.PI) * 0.5,
                    -0.5,
                    -Math.sin(t * Math.PI) * 0.3
            };
        }

        return template;
    }

    // 14. 흩어진 템플릿 (공포 감정용, 5개 이하)
    private double[][] scatterTemplate(int count) {
        double[][] template = new double[count][3];

        // 중앙에 하나, 나머지는 비규칙적으로 흩어짐
        template[0] = new double[]{0, 0, 0};

        Random random = new Random(42); // 일관된 결과를 위한 시드값
        for (int i = 1; i < count; i++) {
            // 비교적 넓게 퍼진 형태
            template[i] = new double[]{
                    (random.nextDouble() - 0.5) * 2,
                    (random.nextDouble() - 0.5) * 2,
                    (random.nextDouble() - 0.5) * 0.5
            };
        }

        return template;
    }

    // 15. 혼돈 템플릿 (공포 감정용, 6-7개)
    private double[][] chaosTemplate(int count) {
        double[][] template = new double[count][3];

        // 불규칙한 3D 구조
        Random random = new Random(43); // 일관된 결과를 위한 시드값
        for (int i = 0; i < count; i++) {
            // 3D 공간에 불규칙하게 분포
            double phi = Math.acos(2 * random.nextDouble() - 1);
            double theta = 2 * Math.PI * random.nextDouble();
            double r = 0.2 + 0.8 * random.nextDouble();

            template[i] = new double[]{
                    r * Math.sin(phi) * Math.cos(theta),
                    r * Math.sin(phi) * Math.sin(theta),
                    r * Math.cos(phi)
            };
        }

        return template;
    }
}
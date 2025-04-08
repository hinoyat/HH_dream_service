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

    // 구의 반경 (전체 우주의 크기)
    private static final double SPHERE_RADIUS = 200.0;

    // 클러스터별 최대 일기 수 (이보다 많으면 여러 별자리로 분할)
    private static final int MAX_DIARIES_PER_CONSTELLATION = 7;

    // 각 일기 간 최소 거리 (충돌 방지용)
    private static final double MIN_DIARY_DISTANCE = 10.0;

    /**
     * 감정에 맞는 별자리 좌표 템플릿을 생성합니다.
     *
     * @param emotion 감정 엔티티
     * @param diaries 배치할 일기 목록
     * @return 생성된 좌표 배열 (각 일기별 [x, y, z] 좌표)
     */
    public double[][] generateConstellationLayout(Emotion emotion, List<Diary> diaries) {
        int count = diaries.size();

        // 일기 수에 따라 적절한 템플릿 선택
        double[][] templatePositions = selectConstellationTemplate(emotion.getName(), count);

        // 템플릿 좌표를 구 표면에 맞게 스케일링 및 위치 조정
        double[][] positions = adjustTemplateToSphere(
                templatePositions,
                emotion.getBaseX(),
                emotion.getBaseY(),
                emotion.getBaseZ(),
                emotion.getBaseRadius()
        );

        return positions;
    }

    /**
     * 여러 그룹의 일기들에 대한 별자리 좌표를 생성합니다.
     * 감정 영역 내에 여러 개의 별자리를 배치합니다.
     *
     * @param emotion             감정 엔티티
     * @param constellationGroups 별자리 그룹 목록 (각 그룹은 하나의 별자리를 형성)
     * @return 그룹별 좌표 배열 맵
     */
    public Map<Integer, double[][]> generateMultipleConstellations(
            Emotion emotion,
            List<List<Diary>> constellationGroups) {

        Map<Integer, double[][]> constellationCoordinates = new HashMap<>();
        int groupCount = constellationGroups.size();

        // 감정 영역 내에 그룹 수만큼 중심점 배치
        double[][] centerPoints = distributePointsOnSphere(
                emotion.getBaseX(),
                emotion.getBaseY(),
                emotion.getBaseZ(),
                emotion.getBaseRadius() * 0.8,  // 감정 영역의 80% 내에 분산
                groupCount
        );

        // 각 그룹별로 별자리 좌표 생성
        for (int i = 0; i < groupCount; i++) {
            List<Diary> group = constellationGroups.get(i);
            if (group.isEmpty()) continue;

            double[] centerPoint = centerPoints[i];

            // 별자리 크기는 그룹 크기에 비례하되 일정 범위 내로 제한
            double constellationScale = Math.min(
                    emotion.getBaseRadius() * 0.4,  // 최대 크기 (감정 반경의 30%)
                    BASE_SCALE * Math.sqrt(group.size() / 3.0)  // 크기 조정
            );

            // 별자리 템플릿 선택 및 좌표 생성
            double[][] templatePositions = selectConstellationTemplate(emotion.getName(), group.size());
            double[][] adjustedPositions = adjustTemplateToSphere(
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
     * 템플릿 좌표를 구 표면에 맞게 조정합니다.
     * 별자리 형태가 더 명확하게 보이도록 개선된 버전
     *
     * @param template 원본 템플릿 좌표
     * @param centerX  중심 X 좌표
     * @param centerY  중심 Y 좌표
     * @param centerZ  중심 Z 좌표
     * @param radius   반경
     * @return 조정된 좌표 배열
     */
    private double[][] adjustTemplateToSphere(
            double[][] template,
            double centerX,
            double centerY,
            double centerZ,
            double radius) {

        int count = template.length;
        double[][] adjusted = new double[count][3];

        // 템플릿 패턴의 평균 중심점 계산
        double avgX = 0, avgY = 0, avgZ = 0;
        for (int i = 0; i < count; i++) {
            avgX += template[i][0];
            avgY += template[i][1];
            avgZ += template[i][2];
        }
        avgX /= count;
        avgY /= count;
        avgZ /= count;

        // 템플릿 패턴 정규화 (중심이 원점에 오도록)
        for (int i = 0; i < count; i++) {
            template[i][0] -= avgX;
            template[i][1] -= avgY;
            template[i][2] -= avgZ;
        }

        // 패턴 크기 계산 (가장 먼 점까지의 거리)
        double maxDist = 0;
        for (int i = 0; i < count; i++) {
            double dist = Math.sqrt(
                    template[i][0] * template[i][0] +
                            template[i][1] * template[i][1] +
                            template[i][2] * template[i][2]);
            maxDist = Math.max(maxDist, dist);
        }

        // 패턴 스케일링 (지정된 반경에 맞게)
        double scale = radius / Math.max(maxDist, 0.001);
        for (int i = 0; i < count; i++) {
            template[i][0] *= scale;
            template[i][1] *= scale;
            template[i][2] *= scale;
        }

        // 원점에서 감정 중심까지의 거리 계산
        double centerDistance = Math.sqrt(centerX * centerX + centerY * centerY + centerZ * centerZ);

        // 센터가 구 중심에 있지 않다면, 구 표면 방향으로의 단위 벡터
        double directionX = centerDistance > 0.001 ? centerX / centerDistance : 0;
        double directionY = centerDistance > 0.001 ? centerY / centerDistance : 0;
        double directionZ = centerDistance > 0.001 ? centerZ / centerDistance : 0;

        // 별자리 회전 행렬 계산 - 별자리 패턴이 감정 영역 중심을 향하도록
        double[][] rotationMatrix = calculateRotationMatrix(directionX, directionY, directionZ);

        // 템플릿을 감정 중심에 배치하고 구 표면으로 투영
        for (int i = 0; i < count; i++) {
            // 회전 적용
            double[] rotated = applyRotation(template[i], rotationMatrix);

            // 감정 중심 위치로 이동
            double x = centerX + rotated[0];
            double y = centerY + rotated[1];
            double z = centerZ + rotated[2];

            // 구 중심에서의 거리 계산
            double distance = Math.sqrt(x*x + y*y + z*z);

            // 구 표면까지의 방향 단위 벡터
            double normX = x / Math.max(distance, 0.001);
            double normY = y / Math.max(distance, 0.001);
            double normZ = z / Math.max(distance, 0.001);

            // 좌표를 구 표면으로 투영 (구 반경 위치)
            adjusted[i][0] = normX * SPHERE_RADIUS;
            adjusted[i][1] = normY * SPHERE_RADIUS;
            adjusted[i][2] = normZ * SPHERE_RADIUS;

            // 각 별자리 점마다 약간의 변화 추가 (별자리가 평면이 아닌 입체적으로 보이도록)
            double variation = 0.97 + 0.03 * Math.sin(i * 0.5);
            adjusted[i][0] *= variation;
            adjusted[i][1] *= variation;
            adjusted[i][2] *= variation;
        }

        return adjusted;
    }

    /**
     * 회전 행렬 계산 - 별자리 패턴이 감정 영역 중심을 향하도록
     */
    private double[][] calculateRotationMatrix(double dirX, double dirY, double dirZ) {
        // 기본 벡터(0,0,1)에서 목표 방향으로의 회전 행렬 계산
        double phi = Math.atan2(dirY, dirX);
        double theta = Math.acos(dirZ);

        double sinPhi = Math.sin(phi);
        double cosPhi = Math.cos(phi);
        double sinTheta = Math.sin(theta);
        double cosTheta = Math.cos(theta);

        // 회전 행렬 구성
        double[][] matrix = new double[3][3];

        matrix[0][0] = cosTheta * cosPhi;
        matrix[0][1] = -sinPhi;
        matrix[0][2] = sinTheta * cosPhi;

        matrix[1][0] = cosTheta * sinPhi;
        matrix[1][1] = cosPhi;
        matrix[1][2] = sinTheta * sinPhi;

        matrix[2][0] = -sinTheta;
        matrix[2][1] = 0;
        matrix[2][2] = cosTheta;

        return matrix;
    }

    /**
     * 점에 회전 행렬 적용
     */
    private double[] applyRotation(double[] point, double[][] matrix) {
        double[] rotated = new double[3];

        rotated[0] = matrix[0][0] * point[0] + matrix[0][1] * point[1] + matrix[0][2] * point[2];
        rotated[1] = matrix[1][0] * point[0] + matrix[1][1] * point[1] + matrix[1][2] * point[2];
        rotated[2] = matrix[2][0] * point[0] + matrix[2][1] * point[1] + matrix[2][2] * point[2];

        return rotated;
    }

    /**
     * 구 표면에 균등하게 분포된 점들을 생성합니다.
     * 별자리 중심점 배치에 사용됩니다.
     */
    private double[][] distributePointsOnSphere(
            double centerX,
            double centerY,
            double centerZ,
            double radius,
            int count) {

        if (count == 1) {
            // 한 개만 필요하면 중앙 방향으로 약간 이동
            return new double[][]{{centerX, centerY, centerZ}};
        }

        double[][] points = new double[count][3];

        // 피보나치 나선 알고리즘으로 구 표면에 균등하게 점 분포
        double goldenRatio = (1 + Math.sqrt(5)) / 2;

        for (int i = 0; i < count; i++) {
            double t = (double) i / count;
            // 골든 앵글에 기반한 계산
            double inclination = Math.acos(1 - 2 * t); // 0 ~ PI (북극에서 남극까지)
            double azimuth = 2 * Math.PI * i / goldenRatio; // 경도 방향 회전

            // 구면 좌표를 직교 좌표로 변환
            double x = Math.sin(inclination) * Math.cos(azimuth);
            double y = Math.sin(inclination) * Math.sin(azimuth);
            double z = Math.cos(inclination);

            // 구 중심에서 표면 쪽으로의 방향 벡터에 반경을 곱함
            points[i][0] = centerX + radius * x;
            points[i][1] = centerY + radius * y;
            points[i][2] = centerZ + radius * z;
        }

        return points;
    }

    /**
     * 감정 이름과 일기 수에 따라 적절한 별자리 템플릿을 선택합니다.
     * 다양한 별자리 패턴을 제공하여 자연스러운 형태를 만듭니다.
     *
     * @param emotionName 감정 이름
     * @param count       일기 수
     * @return 선택된 템플릿 좌표 배열
     */
    private double[][] selectConstellationTemplate(String emotionName, int count) {
        // 일기 수가 정해진 범위를 벗어나면 조정
        count = Math.min(Math.max(count, 2), MAX_DIARIES_PER_CONSTELLATION);

        // 다양성을 위한 시드 생성
        int seed = (emotionName.hashCode() + 31 * count);
        Random random = new Random(seed);

        // 다양한 패턴 유형 정의
        List<PatternType> patternTypes = new ArrayList<>();

        // 실제 별자리 패턴 (10개)
        patternTypes.add(PatternType.ORION);          // 오리온자리
        patternTypes.add(PatternType.BIG_DIPPER);     // 큰곰자리(북두칠성)
        patternTypes.add(PatternType.CASSIOPEIA);     // 카시오페이아자리
        patternTypes.add(PatternType.GEMINI);         // 쌍둥이자리
        patternTypes.add(PatternType.LIBRA);          // 천칭자리
        patternTypes.add(PatternType.SOUTHERN_CROSS); // 남십자자리
        patternTypes.add(PatternType.CORONA);         // 왕관자리
        patternTypes.add(PatternType.PERSEUS);        // 페르세우스자리
        patternTypes.add(PatternType.LYRA);           // 거문고자리
        patternTypes.add(PatternType.SAGITTARIUS);    // 궁수자리

        // 기하학적 패턴 (10개)
        patternTypes.add(PatternType.STAR);           // 별 모양
        patternTypes.add(PatternType.CIRCLE);         // 원형
        patternTypes.add(PatternType.TRIANGLE);       // 삼각형
        patternTypes.add(PatternType.ARROW);          // 화살표
        patternTypes.add(PatternType.ZIGZAG);         // 지그재그
        patternTypes.add(PatternType.WAVE);           // 파도 모양
        patternTypes.add(PatternType.HEXAGON);        // 육각형
        patternTypes.add(PatternType.SPIRAL);         // 나선형
        patternTypes.add(PatternType.CROSS);          // 십자가
        patternTypes.add(PatternType.SCATTER);        // 흩어진 형태

        // 일기 수에 맞는 패턴만 필터링
        List<PatternType> suitablePatterns = new ArrayList<>();
        for (PatternType type : patternTypes) {
            if (isPatternSuitableForCount(type, count)) {
                suitablePatterns.add(type);
            }
        }

        // 적합한 패턴이 없다면 기본 패턴 사용
        if (suitablePatterns.isEmpty()) {
            if (count <= 3) return createTrianglePattern(count);
            else if (count <= 5) return createStarPattern(count);
            else return createCirclePattern(count);
        }

        // 감정별 선호도 가중치 적용
        Map<PatternType, Integer> emotionWeights = getEmotionPatternWeights(emotionName);

        // 가중치에 따른 랜덤 선택
        int totalWeight = 0;
        Map<PatternType, Integer> patternWeights = new HashMap<>();

        for (PatternType type : suitablePatterns) {
            int weight = 1; // 기본 가중치

            // 감정별 선호도 추가
            if (emotionWeights.containsKey(type)) {
                weight += emotionWeights.get(type);
            }

            patternWeights.put(type, weight);
            totalWeight += weight;
        }

        // 가중치 기반 랜덤 선택
        int randomWeight = random.nextInt(totalWeight);
        int weightSum = 0;
        PatternType selectedType = suitablePatterns.get(0); // 기본값

        for (Map.Entry<PatternType, Integer> entry : patternWeights.entrySet()) {
            weightSum += entry.getValue();
            if (randomWeight < weightSum) {
                selectedType = entry.getKey();
                break;
            }
        }

        // 선택된 패턴 생성
        double[][] baseTemplate = createPatternByType(selectedType, count);

        // 패턴에 약간의 랜덤 변형 적용
        return applyRandomVariation(baseTemplate, random.nextInt(100000));
    }

    /**
     * 감정별 패턴 선호도 가중치 설정
     */
    private Map<PatternType, Integer> getEmotionPatternWeights(String emotionName) {
        Map<PatternType, Integer> weights = new HashMap<>();

        // 감정별로 선호하는 패턴에 더 높은 가중치 부여 (선택 확률 높임)
        switch (emotionName) {
            case "행복":
                weights.put(PatternType.STAR, 5);      // 별 모양 선호
                weights.put(PatternType.CIRCLE, 4);    // 원형 선호
                weights.put(PatternType.CORONA, 3);    // 왕관자리 선호
                weights.put(PatternType.LYRA, 3);      // 거문고자리 선호
                break;
            case "슬픔":
                weights.put(PatternType.WAVE, 4);      // 파도 모양 선호
                weights.put(PatternType.SOUTHERN_CROSS, 3); // 남십자자리 선호
                weights.put(PatternType.CASSIOPEIA, 3); // 카시오페이아자리 선호
                break;
            case "분노":
                weights.put(PatternType.ZIGZAG, 5);    // 지그재그 선호
                weights.put(PatternType.ARROW, 4);     // 화살표 선호
                weights.put(PatternType.SAGITTARIUS, 3); // 궁수자리 선호
                break;
            case "불안":
                weights.put(PatternType.SCATTER, 5);    // 흩어진 형태 선호
                weights.put(PatternType.SPIRAL, 4);     // 나선형 선호
                weights.put(PatternType.PERSEUS, 3);    // 페르세우스자리 선호
                break;
            case "평화":
                weights.put(PatternType.CIRCLE, 5);     // 원형 선호
                weights.put(PatternType.LIBRA, 4);      // 천칭자리 선호
                weights.put(PatternType.WAVE, 3);       // 파도 모양 선호
                break;
            case "희망":
                weights.put(PatternType.STAR, 4);       // 별 모양 선호
                weights.put(PatternType.ORION, 3);      // 오리온자리 선호
                weights.put(PatternType.CORONA, 3);     // 왕관자리 선호
                break;
            case "공포":
                weights.put(PatternType.SCATTER, 5);    // 흩어진 형태 선호
                weights.put(PatternType.CROSS, 4);      // 십자가 선호
                weights.put(PatternType.ZIGZAG, 3);     // 지그재그 선호
                break;
            default:
                // 다른 감정은 해시 기반으로 랜덤 선호도 부여
                PatternType[] allTypes = PatternType.values();
                weights.put(allTypes[Math.abs(emotionName.hashCode()) % allTypes.length], 4);
                weights.put(allTypes[Math.abs(emotionName.hashCode() + 1) % allTypes.length], 3);
                break;
        }

        return weights;
    }

    /**
     * 패턴 유형이 일기 수에 적합한지 확인
     */
    private boolean isPatternSuitableForCount(PatternType type, int count) {
        switch (type) {
            case ORION:          return count >= 5 && count <= 7;
            case BIG_DIPPER:     return count == 7;
            case CASSIOPEIA:     return count >= 5 && count <= 7;
            case GEMINI:         return count >= 6 && count <= 7;
            case LIBRA:          return count >= 4 && count <= 6;
            case SOUTHERN_CROSS: return count == 4 || count == 5;
            case CORONA:         return count >= 5 && count <= 7;
            case PERSEUS:        return count >= 4 && count <= 7;
            case LYRA:           return count >= 4 && count <= 6;
            case SAGITTARIUS:    return count >= 5 && count <= 7;
            case STAR:           return count >= 5 && count <= 7;
            case CIRCLE:         return count >= 3;
            case TRIANGLE:       return count >= 3 && count <= 6;
            case ARROW:          return count >= 3 && count <= 7;
            case ZIGZAG:         return count >= 3;
            case WAVE:           return count >= 3;
            case HEXAGON:        return count == 6 || count == 7;
            case SPIRAL:         return count >= 4;
            case CROSS:          return count >= 5 && count <= 7;
            case SCATTER:        return count >= 3;
            default:             return true;
        }
    }

    /**
     * 패턴 유형 열거
     */
    private enum PatternType {
        // 실제 별자리
        ORION, BIG_DIPPER, CASSIOPEIA, GEMINI, LIBRA, SOUTHERN_CROSS, CORONA, PERSEUS, LYRA, SAGITTARIUS,
        // 기하학적 패턴
        STAR, CIRCLE, TRIANGLE, ARROW, ZIGZAG, WAVE, HEXAGON, SPIRAL, CROSS, SCATTER
    }

    /**
     * 선택된 패턴 유형에 따라 해당 패턴 생성
     */
    private double[][] createPatternByType(PatternType type, int count) {
        switch (type) {
            case ORION:          return createOrionPattern(count);
            case BIG_DIPPER:     return createBigDipperPattern(count);
            case CASSIOPEIA:     return createCassiopeiaPattern(count);
            case GEMINI:         return createGeminiPattern(count);
            case LIBRA:          return createLibraPattern(count);
            case SOUTHERN_CROSS: return createSouthernCrossPattern(count);
            case CORONA:         return createCoronaPattern(count);
            case PERSEUS:        return createPerseusPattern(count);
            case LYRA:           return createLyraPattern(count);
            case SAGITTARIUS:    return createSagittariusPattern(count);
            case STAR:           return createStarPattern(count);
            case CIRCLE:         return createCirclePattern(count);
            case TRIANGLE:       return createTrianglePattern(count);
            case ARROW:          return createArrowPattern(count);
            case ZIGZAG:         return createZigzagPattern(count);
            case WAVE:           return createWavePattern(count);
            case HEXAGON:        return createHexagonPattern(count);
            case SPIRAL:         return createSpiralPattern(count);
            case CROSS:          return createCrossPattern(count);
            case SCATTER:        return createScatterPattern(count);
            default:             return createStarPattern(count);
        }
    }

    /**
     * 기본 템플릿에 랜덤 변형을 적용하여 다양성 증가
     */
    private double[][] applyRandomVariation(double[][] template, int seed) {
        Random random = new Random(seed);
        double[][] result = new double[template.length][3];

        // 변형 정도 설정 (0.05 ~ 0.15 사이의 랜덤 값)
        double variationAmount = 0.05 + random.nextDouble() * 0.1;

        // 각 점마다 변형 적용
        for (int i = 0; i < template.length; i++) {
            // x, y 좌표에 약간의 변형 적용
            result[i][0] = template[i][0] * (1 + variationAmount * (random.nextDouble() - 0.5));
            result[i][1] = template[i][1] * (1 + variationAmount * (random.nextDouble() - 0.5));

            // z 좌표에도 약간의 변형 적용
            result[i][2] = template[i][2] + variationAmount * 0.5 * (random.nextDouble() - 0.5);
        }

        // 추가 변형: 전체 회전 적용 여부 (25% 확률)
        if (random.nextDouble() < 0.25) {
            double rotationAngle = random.nextDouble() * Math.PI / 2; // 0~90도 사이 회전
            result = rotatePattern(result, rotationAngle);
        }

        return result;
    }

    /**
     * 패턴을 Z축 중심으로 회전
     */
    private double[][] rotatePattern(double[][] pattern, double angle) {
        double[][] rotated = new double[pattern.length][3];
        double sin = Math.sin(angle);
        double cos = Math.cos(angle);

        for (int i = 0; i < pattern.length; i++) {
            double x = pattern[i][0];
            double y = pattern[i][1];

            rotated[i][0] = x * cos - y * sin;
            rotated[i][1] = x * sin + y * cos;
            rotated[i][2] = pattern[i][2]; // z 좌표는 변경 없음
        }

        return rotated;
    }

    /**
     * 오리온자리 패턴 생성
     */
    private double[][] createOrionPattern(int count) {
        double[][] template = new double[count][3];

        // 오리온자리 주요 별 좌표 (표준화된 형태)
        double[][] orionStars = {
                {0, 1.0, 0},     // 베텔게우스 (오른쪽 어깨)
                {-0.8, 0.8, 0},  // 벨라트릭스 (왼쪽 어깨)
                {-0.2, 0.2, 0},  // 알니람 (벨트 왼쪽)
                {0, 0, 0},       // 알니탁 (벨트 중앙)
                {0.2, -0.2, 0},  // 민타카 (벨트 오른쪽)
                {-0.5, -0.9, 0}, // 싸이프 (왼쪽 다리)
                {0.7, -1.0, 0}   // 리겔 (오른쪽 다리)
        };

        // 사용 가능한 별 좌표만 복사
        int usableStars = Math.min(count, orionStars.length);
        for (int i = 0; i < usableStars; i++) {
            template[i] = orionStars[i].clone();
        }

        // 추가 별이 필요한 경우 기존 별 주변에 배치
        if (count > orionStars.length) {
            Random random = new Random(count);
            for (int i = orionStars.length; i < count; i++) {
                int baseIndex = i % orionStars.length;
                template[i][0] = orionStars[baseIndex][0] + (random.nextDouble() - 0.5) * 0.3;
                template[i][1] = orionStars[baseIndex][1] + (random.nextDouble() - 0.5) * 0.3;
                template[i][2] = orionStars[baseIndex][2] + (random.nextDouble() - 0.5) * 0.1;
            }
        }

        return template;
    }

    /**
     * 큰곰자리(북두칠성) 패턴 생성
     */
    private double[][] createBigDipperPattern(int count) {
        double[][] template = new double[count][3];

        // 북두칠성 좌표
        double[][] dipperStars = {
                {-0.8, -0.2, 0},  // 두베
                {-0.4, 0, 0},     // 메라크
                {0, 0.2, 0},      // 페크다
                {0.4, 0.2, 0},    // 메그레즈
                {0.7, 0.4, 0},    // 알리오스
                {1.0, 0.2, 0},    // 미자르
                {1.2, 0.3, 0}     // 알카이드
        };

        // 사용 가능한 별 복사
        int usableStars = Math.min(count, dipperStars.length);
        for (int i = 0; i < usableStars; i++) {
            template[i] = dipperStars[i].clone();
        }

        // 7개보다 적은 경우에는 핵심 별들만 사용
        if (count < dipperStars.length) {
            // 지금은 순서대로 가져오지만, 필요하면 중요한 별들만 선택하도록 수정 가능
        }

        return template;
    }

    /**
     * 카시오페이아자리 패턴 생성 (W 또는 M 모양)
     */
    private double[][] createCassiopeiaPattern(int count) {
        double[][] template = new double[count][3];

        // 카시오페이아자리 주요 별 5개 (W 모양)
        double[][] cassiopeiaStars = {
                {-0.8, -0.2, 0},  // 세파
                {-0.4, 0.4, 0},   // 쉐다르
                {0, -0.2, 0},     // 감마
                {0.4, 0.4, 0},    // 룩바
                {0.8, -0.2, 0}    // 세게인
        };

        // 사용 가능한 별 복사
        int usableStars = Math.min(count, cassiopeiaStars.length);
        for (int i = 0; i < usableStars; i++) {
            template[i] = cassiopeiaStars[i].clone();
        }

        // 추가 별이 필요한 경우 W 모양을 확장
        if (count > cassiopeiaStars.length) {
            Random random = new Random(count);
            for (int i = cassiopeiaStars.length; i < count; i++) {
                // W 패턴 확장
                if (i == 5) {
                    template[i][0] = 1.2;
                    template[i][1] = 0.3;
                    template[i][2] = 0;
                } else if (i == 6) {
                    template[i][0] = -1.2;
                    template[i][1] = 0.3;
                    template[i][2] = 0;
                } else {
                    int baseIndex = i % cassiopeiaStars.length;
                    template[i][0] = cassiopeiaStars[baseIndex][0] + (random.nextDouble() - 0.5) * 0.3;
                    template[i][1] = cassiopeiaStars[baseIndex][1] + (random.nextDouble() - 0.5) * 0.3;
                    template[i][2] = cassiopeiaStars[baseIndex][2] + (random.nextDouble() - 0.5) * 0.1;
                }
            }
        }

        return template;
    }

    /**
     * 쌍둥이자리 패턴 생성
     */
    private double[][] createGeminiPattern(int count) {
        double[][] template = new double[count][3];

        // 쌍둥이자리 주요 별 (두 사람 모양)
        double[][] geminiStars = {
                {-0.5, 1.0, 0},   // 카스토르(머리)
                {-0.6, 0.6, 0},   // 왼쪽 어깨
                {-0.7, 0.2, 0},   // 왼쪽 발

                {0.5, 0.9, 0},    // 폴룩스(머리)
                {0.6, 0.5, 0},    // 오른쪽 어깨
                {0.7, 0.1, 0},    // 오른쪽 발

                {0, -0.4, 0}      // 중앙 별
        };

        // 사용 가능한 별 복사
        int usableStars = Math.min(count, geminiStars.length);
        for (int i = 0; i < usableStars; i++) {
            template[i] = geminiStars[i].clone();
        }

        // 더 적은 수의 별이면 핵심 별만 사용
        if (count < geminiStars.length) {
            // 중요도 순으로 이미 정렬되어 있다고 가정
        }

        return template;
    }

    /**
     * 천칭자리 패턴 생성
     */
    private double[][] createLibraPattern(int count) {
        double[][] template = new double[count][3];

        // 천칭자리 주요 별 (저울 모양)
        double[][] libraStars = {
                {0, 0.8, 0},      // 상단
                {-0.8, 0, 0},     // 왼쪽
                {0, 0, 0},        // 중앙
                {0.8, 0, 0},      // 오른쪽
                {0, -0.8, 0},     // 하단
                {-0.4, -0.4, 0}   // 왼쪽 하단 보조
        };

        // 사용 가능한 별 복사
        int usableStars = Math.min(count, libraStars.length);
        for (int i = 0; i < usableStars; i++) {
            template[i] = libraStars[i].clone();
        }

        return template;
    }

    /**
     * 남십자자리 패턴 생성
     */
    private double[][] createSouthernCrossPattern(int count) {
        double[][] template = new double[count][3];

        // 남십자자리 주요 별 (십자가 모양)
        double[][] crossStars = {
                {0, 1.0, 0},      // 상단
                {-0.6, 0, 0},     // 왼쪽
                {0, 0, 0},        // 중앙
                {0.6, 0, 0},      // 오른쪽
                {0, -1.0, 0}      // 하단
        };

        // 사용 가능한 별 복사
        int usableStars = Math.min(count, crossStars.length);
        for (int i = 0; i < usableStars; i++) {
            template[i] = crossStars[i].clone();
        }

        return template;
    }

    /**
     * 왕관자리 패턴 생성
     */
    private double[][] createCoronaPattern(int count) {
        double[][] template = new double[count][3];

        // 왕관 모양의 별 배치
        double[][] coronaStars = {
                {-0.8, 0, 0},     // 왼쪽 끝
                {-0.4, 0.4, 0},   // 왼쪽 상단
                {0, 0.7, 0},      // 중앙 상단
                {0.4, 0.4, 0},    // 오른쪽 상단
                {0.8, 0, 0},      // 오른쪽 끝
                {0, 0, 0},        // 중앙
                {0, -0.5, 0}      // 하단
        };

        // 사용 가능한 별 복사
        int usableStars = Math.min(count, coronaStars.length);
        for (int i = 0; i < usableStars; i++) {
            template[i] = coronaStars[i].clone();
        }

        return template;
    }

    /**
     * 페르세우스자리 패턴 생성
     */
    private double[][] createPerseusPattern(int count) {
        double[][] template = new double[count][3];

        // 페르세우스자리 주요 별
        double[][] perseusStars = {
                {0, 0.8, 0},      // 머리
                {0.3, 0.4, 0},    // 오른쪽 어깨
                {-0.3, 0.4, 0},   // 왼쪽 어깨
                {0.1, 0, 0},      // 허리
                {-0.5, -0.3, 0},  // 왼쪽 다리
                {0.5, -0.5, 0},   // 오른쪽 다리
                {-0.8, -0.7, 0}   // 발
        };

        // 사용 가능한 별 복사
        int usableStars = Math.min(count, perseusStars.length);
        for (int i = 0; i < usableStars; i++) {
            template[i] = perseusStars[i].clone();
        }

        return template;
    }

    /**
     * 거문고자리 패턴 생성
     */
    private double[][] createLyraPattern(int count) {
        double[][] template = new double[count][3];

        // 거문고자리 주요 별 (거문고 모양)
        double[][] lyraStars = {
                {0, 1.0, 0},      // 베가 (꼭대기)
                {-0.4, 0.5, 0},   // 왼쪽 위
                {0.4, 0.5, 0},    // 오른쪽 위
                {-0.3, 0, 0},     // 왼쪽 중간
                {0.3, 0, 0},      // 오른쪽 중간
                {0, -0.5, 0}      // 하단
        };

        // 사용 가능한 별 복사
        int usableStars = Math.min(count, lyraStars.length);
        for (int i = 0; i < usableStars; i++) {
            template[i] = lyraStars[i].clone();
        }

        return template;
    }

    /**
     * 궁수자리 패턴 생성
     */
    private double[][] createSagittariusPattern(int count) {
        double[][] template = new double[count][3];

        // 궁수자리 주요 별 (화살과 활 모양)
        double[][] sagittariusStars = {
                {0, 0.8, 0},      // 상단 화살촉
                {-0.3, 0.5, 0},   // 화살 왼쪽 날개
                {0.3, 0.5, 0},    // 화살 오른쪽 날개
                {0, 0.2, 0},      // 활 중앙
                {-0.5, 0, 0},     // 활 왼쪽
                {0.5, 0, 0},      // 활 오른쪽
                {0, -0.5, 0}      // 하단
        };

        // 사용 가능한 별 복사
        int usableStars = Math.min(count, sagittariusStars.length);
        for (int i = 0; i < usableStars; i++) {
            template[i] = sagittariusStars[i].clone();
        }

        return template;
    }

    /**
     * 나선형 패턴 생성
     */
    private double[][] createSpiralPattern(int count) {
        double[][] template = new double[count][3];

        // 나선형 패턴 생성
        for (int i = 0; i < count; i++) {
            double angle = Math.PI * 2 * i / count + (i * 0.3); // 점점 회전 증가
            double radius = 0.2 + 0.8 * i / (count - 1); // 반경 점점 증가

            template[i][0] = radius * Math.cos(angle);
            template[i][1] = radius * Math.sin(angle);
            template[i][2] = 0.1 * i / count; // Z축 약간 변화
        }

        return template;
    }

    /**
     * 십자가 패턴 생성
     */
    private double[][] createCrossPattern(int count) {
        double[][] template = new double[count][3];

        if (count < 5) {
            return createSouthernCrossPattern(count); // 5개 미만이면 남십자자리 형태로
        }

        // 십자가 기본 형태
        double[][] crossPoints = {
                {0, 1.0, 0},      // 상단
                {-0.8, 0, 0},     // 왼쪽
                {0, 0, 0},        // 중앙
                {0.8, 0, 0},      // 오른쪽
                {0, -1.0, 0},     // 하단
                {-0.4, 0.5, 0},   // 왼쪽 상단
                {0.4, 0.5, 0}     // 오른쪽 상단
        };

        // 사용 가능한 점 복사
        int usablePoints = Math.min(count, crossPoints.length);
        for (int i = 0; i < usablePoints; i++) {
            template[i] = crossPoints[i].clone();
        }

        // Z축에 약간의 변화 추가
        for (int i = 0; i < count; i++) {
            template[i][2] = 0.1 * Math.sin(i * 1.2);
        }

        return template;
    }

    /**
     * 육각형 패턴 생성
     */
    private double[][] createHexagonPattern(int count) {
        double[][] template = new double[count][3];

        if (count < 3) {
            template[0] = new double[]{-1.0, 0, 0};
            if (count > 1) template[1] = new double[]{1.0, 0, 0};
            return template;
        }

        // 육각형 기본 모양
        double[][] hexagonPoints = {
                {0, 1.0, 0},        // 상단
                {-0.87, 0.5, 0},    // 왼쪽 상단
                {-0.87, -0.5, 0},   // 왼쪽 하단
                {0, -1.0, 0},       // 하단
                {0.87, -0.5, 0},    // 오른쪽 하단
                {0.87, 0.5, 0},     // 오른쪽 상단
                {0, 0, 0}           // 중앙 (7개 필요시)
        };

        // 사용 가능한 점 복사
        int usablePoints = Math.min(count, hexagonPoints.length);
        for (int i = 0; i < usablePoints; i++) {
            template[i] = hexagonPoints[i].clone();
        }

        // Z축에 약간의 변화 추가
        for (int i = 0; i < count; i++) {
            template[i][2] = 0.1 * Math.sin(i * 1.0);
        }

        return template;
    }

    /**
     * 기본 별 모양 패턴 개선 버전
     */
    private double[][] createStarPattern(int count) {
        double[][] template = new double[count][3];

        if (count < 3) {
            // 2개 이하면 간단한 선
            template[0] = new double[]{-1.0, 0, 0};
            if (count > 1) template[1] = new double[]{1.0, 0, 0};
            return template;
        }

        // 중심점이 있는 별 모양
        boolean hasCenterPoint = count % 2 == 1;
        int pointsOnCircle = hasCenterPoint ? count - 1 : count;

        // 중심점 설정
        if (hasCenterPoint) {
            template[0] = new double[]{0, 0, 0.1}; // 중심은 약간 앞으로
        }

        // 별 꼭지점 배치
        int startIdx = hasCenterPoint ? 1 : 0;
        for (int i = 0; i < pointsOnCircle; i++) {
            double angle = 2 * Math.PI * i / pointsOnCircle;
            // 별 모양 - 각도에 따라 반경 변화
            double radius = 1.0 + 0.3 * Math.sin(angle * (pointsOnCircle / 2));

            template[startIdx + i] = new double[]{
                    radius * Math.cos(angle),
                    radius * Math.sin(angle),
                    0.1 * Math.sin(angle * 2) // Z축에 약간의 변화
            };
        }

        return template;
    }

    /**
     * 원형 패턴 생성
     */
    private double[][] createCirclePattern(int count) {
        double[][] template = new double[count][3];

        if (count < 3) {
            // 2개 이하면 간단한 선
            template[0] = new double[]{-1.0, 0, 0};
            if (count > 1) template[1] = new double[]{1.0, 0, 0};
            return template;
        }

        // 중심점이 있는 원형 (일기가 많으면 중심점 추가)
        boolean hasCenterPoint = count > 5;
        int pointsOnCircle = hasCenterPoint ? count - 1 : count;

        // 중심점 설정
        if (hasCenterPoint) {
            template[0] = new double[]{0, 0, 0.1}; // 중심은 약간 앞으로
        }

        // 원형 배치
        int startIdx = hasCenterPoint ? 1 : 0;
        for (int i = 0; i < pointsOnCircle; i++) {
            double angle = 2 * Math.PI * i / pointsOnCircle;

            template[startIdx + i] = new double[]{
                    Math.cos(angle),
                    Math.sin(angle),
                    0.15 * Math.sin(i * 0.7) // Z축에 약간의 변화
            };
        }

        return template;
    }

    /**
     * 삼각형 패턴 생성
     */
    private double[][] createTrianglePattern(int count) {
        double[][] template = new double[count][3];

        if (count < 3) {
            // 2개 이하면 간단한 선
            template[0] = new double[]{-1.0, 0, 0};
            if (count > 1) template[1] = new double[]{1.0, 0, 0};
            return template;
        }

        // 기본 삼각형
        template[0] = new double[]{0, 1.0, 0}; // 위
        template[1] = new double[]{-0.9, -0.5, 0}; // 왼쪽 아래
        template[2] = new double[]{0.9, -0.5, 0}; // 오른쪽 아래

        // 추가 점들은 삼각형 내부나 주변에 배치
        if (count > 3) {
            template[3] = new double[]{0, 0, 0}; // 중앙
        }

        if (count > 4) {
            template[4] = new double[]{-0.45, 0.25, 0}; // 왼쪽 중간
        }

        if (count > 5) {
            template[5] = new double[]{0.45, 0.25, 0}; // 오른쪽 중간
        }

        if (count > 6) {
            template[6] = new double[]{0, -0.5, 0}; // 아래 중간
        }

        // Z축에 약간의 변화 추가
        for (int i = 0; i < count; i++) {
            template[i][2] = 0.1 * Math.sin(i * 1.0);
        }

        return template;
    }

    /**
     * 화살표 모양 패턴 생성
     */
    private double[][] createArrowPattern(int count) {
        double[][] template = new double[count][3];

        if (count < 3) {
            // 2개 이하면 간단한 선
            template[0] = new double[]{-1.0, 0, 0};
            if (count > 1) template[1] = new double[]{1.0, 0, 0};
            return template;
        }

        // 화살표 모양 생성
        template[0] = new double[]{0, 1.2, 0}; // 화살 머리
        template[1] = new double[]{-0.8, 0, 0}; // 왼쪽 날개
        template[2] = new double[]{0.8, 0, 0}; // 오른쪽 날개

        if (count > 3) {
            template[3] = new double[]{0, 0.5, 0}; // 중간 지점
        }

        if (count > 4) {
            template[4] = new double[]{0, -0.7, 0}; // 화살 꼬리
        }

        if (count > 5) {
            template[5] = new double[]{-0.4, -0.3, 0}; // 꼬리 왼쪽
        }

        if (count > 6) {
            template[6] = new double[]{0.4, -0.3, 0}; // 꼬리 오른쪽
        }

        // Z축에 약간의 변화 추가
        for (int i = 0; i < count; i++) {
            template[i][2] = 0.1 * Math.sin(i * 1.2);
        }

        return template;
    }

    /**
     * 지그재그 모양 패턴 생성
     */
    private double[][] createZigzagPattern(int count) {
        double[][] template = new double[count][3];

        if (count < 3) {
            // 2개 이하면 간단한 선
            template[0] = new double[]{-1.0, 0, 0};
            if (count > 1) template[1] = new double[]{1.0, 0, 0};
            return template;
        }

        // 지그재그 모양 생성
        double stepX = 2.0 / (count - 1);

        for (int i = 0; i < count; i++) {
            double x = -1.0 + i * stepX;
            double y = 0.7 * ((i % 2 == 0) ? 1 : -1);

            template[i] = new double[]{
                    x,
                    y,
                    0.1 * Math.sin(i * 0.8) // Z축에 약간의 변화
            };
        }

        return template;
    }

    /**
     * 파도 모양 패턴 생성
     */
    private double[][] createWavePattern(int count) {
        double[][] template = new double[count][3];

        if (count < 3) {
            // 2개 이하면 간단한 선
            template[0] = new double[]{-1.0, 0, 0};
            if (count > 1) template[1] = new double[]{1.0, 0, 0};
            return template;
        }

        // 파도 모양 생성
        double stepX = 2.0 / (count - 1);

        for (int i = 0; i < count; i++) {
            double x = -1.0 + i * stepX;
            double phase = Math.PI * i / (count - 1);
            double y = 0.7 * Math.sin(phase * 2);

            template[i] = new double[]{
                    x,
                    y,
                    0.1 * Math.cos(phase * 3) // Z축에 약간의 변화
            };
        }

        return template;
    }

    /**
     * 흩어진 모양 패턴 생성
     */
    private double[][] createScatterPattern(int count) {
        double[][] template = new double[count][3];

        if (count < 3) {
            // 2개 이하면 간단한 선
            template[0] = new double[]{-1.0, 0, 0};
            if (count > 1) template[1] = new double[]{1.0, 0, 0};
            return template;
        }

        // 중심점
        template[0] = new double[]{0, 0, 0};

        // 나머지 점들을 중심 주변에 흩어놓기
        // 일관된 결과를 위해 고정된 시드 사용
        Random random = new Random(count);

        for (int i = 1; i < count; i++) {
            double angle = 2 * Math.PI * random.nextDouble();
            double radius = 0.5 + 0.7 * random.nextDouble();
            double z = 0.3 * (random.nextDouble() - 0.5);

            template[i] = new double[]{
                    radius * Math.cos(angle),
                    radius * Math.sin(angle),
                    z
            };
        }

        return template;
    }

}
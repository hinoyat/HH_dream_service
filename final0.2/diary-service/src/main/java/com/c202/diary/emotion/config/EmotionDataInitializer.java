package com.c202.diary.emotion.config;

import com.c202.diary.emotion.entity.Emotion;
import com.c202.diary.emotion.repository.EmotionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class EmotionDataInitializer implements CommandLineRunner {

    private final EmotionRepository emotionRepository;

    // 구의 반경 (전체 우주의 크기)
    private static final double SPHERE_RADIUS = 200.0;

    @Override
    public void run(String... args) {
        if (emotionRepository.count() == 0) {
            log.info("감정 생성 중");
            initializeEmotions();
            log.info("감정 생성 완료");
        } else {
            log.info("이미 DB에 존재합니다.");
        }
    }

    private void initializeEmotions() {
        // 기본 감정 목록 (총 7개)
        String[] emotionNames = {
                "행복", "슬픔", "분노", "불안", "평화", "희망", "공포"
        };

        int totalEmotions = emotionNames.length;
        List<Emotion> emotions = new ArrayList<>();

        // 7개 감정을 Spherical Fibonacci 방식으로 구 표면에 균등 분포된 점으로 배치
        double[][] positions = generateEvenlyDistributedPoints(totalEmotions);

        for (int i = 0; i < totalEmotions; i++) {
            String emotionName = emotionNames[i];

            // 단위 구상의 좌표에 SPHERE_RADIUS를 곱하여 실제 좌표 계산
            double x = positions[i][0] * SPHERE_RADIUS;
            double y = positions[i][1] * SPHERE_RADIUS;
            double z = positions[i][2] * SPHERE_RADIUS;

            // 감정 영역 반경은 기본적으로 전체 구 반경의 50%로 설정 (감정별로 미세 조정)
            double emotionRadius = calculateEmotionRadius(emotionName, SPHERE_RADIUS);

            emotions.add(Emotion.builder()
                    .name(emotionName)
                    .baseX(x)
                    .baseY(y)
                    .baseZ(z)
                    .baseRadius(emotionRadius)
                    .diaryCount(0)
                    .build());
        }

        emotionRepository.saveAll(emotions);

        // 로그로 생성된 감정 정보 확인
        for (Emotion emotion : emotions) {
            log.info("생성된 감정: {} position ({}, {}, {}) radius {}",
                    emotion.getName(), emotion.getBaseX(), emotion.getBaseY(),
                    emotion.getBaseZ(), emotion.getBaseRadius());
        }
    }

    /**
     * 구 표면에 균등하게 분포된 7개의 점을 Spherical Fibonacci 방식을 이용해 생성합니다.
     * 이 방식은 북극부터 남극까지 y 좌표가 점진적으로 분포하고,
     * 황금각을 이용해 x, z 좌표가 자연스럽게 배열됩니다.
     *
     * @param count 감정 개수 (반드시 7)
     * @return 정규화된 3D 좌표 배열 (각 점은 [x, y, z] 형태; 단위 구상의 좌표)
     */
    private double[][] generateEvenlyDistributedPoints(int count) {
        if (count != 7) {
            throw new IllegalArgumentException("이 구현은 정확히 7개의 감정만 지원합니다.");
        }
        double[][] positions = new double[count][3];
        double goldenAngle = Math.PI * (3 - Math.sqrt(5));  // 약 2.39996

        for (int i = 0; i < count; i++) {
            // y 값을 1에서 -1까지 선형 분포: (0,1,...,6) -> y = 1 - 2*i/(n-1)
            double y = 1 - 2.0 * i / (count - 1);
            double radius = Math.sqrt(Math.max(0, 1 - y * y));
            double theta = i * goldenAngle;
            double x = Math.cos(theta) * radius;
            double z = Math.sin(theta) * radius;
            positions[i][0] = x;
            positions[i][1] = y;
            positions[i][2] = z;
        }
        return positions;
    }

    /**
     * 감정별 특성에 따라 영역 반경을 계산합니다.
     * 기본적으로 전체 구 반경의 50%를 기준으로 감정별 미세 조정을 적용합니다.
     *
     * @param emotionName 감정 이름
     * @param baseRadius 전체 구 반경
     * @return 계산된 감정 영역 반경
     */
    private double calculateEmotionRadius(String emotionName, double baseRadius) {
        double radius = baseRadius * 0.5;
        switch (emotionName) {
            case "행복":
                radius *= 1.15;
                break;
            case "불안":
                radius *= 1.25;
                break;
            case "공포":
                radius *= 1.2;
                break;
            case "평화":
                radius *= 0.95;
                break;
            case "희망":
                radius *= 1.05;
                break;
            case "슬픔":
                radius *= 1.0;
                break;
            case "분노":
                radius *= 0.9;
                break;
            default:
                break;
        }
        return radius;
    }
}

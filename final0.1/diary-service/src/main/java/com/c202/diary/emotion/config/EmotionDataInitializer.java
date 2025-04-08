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
        // 기본 감정 목록
        String[] emotionNames = {
                "행복", "슬픔", "분노", "불안", "평화", "희망", "공포"
        };

        int totalEmotions = emotionNames.length;
        double radius = 150.0;

        List<Emotion> emotions = new ArrayList<>();

        // 피보나치 나선 분포 알고리즘을 사용하여 구 표면에 균등하게 배치
        // 이 방식은 구 표면에 점들을 균등하게 배치하는 가장 좋은 방법 중 하나입니다
        double goldenRatio = (1 + Math.sqrt(5)) / 2;

        for (int i = 0; i < totalEmotions; i++) {
            // 골든 앵글에 기반한 계산
            double t = (double) i / totalEmotions;
            double inclination = Math.acos(1 - 2 * t); // 0 ~ PI (북극에서 남극까지)
            double azimuth = 2 * Math.PI * i / goldenRatio; // 경도 방향 회전

            // 감정별 위치 조정 - 각 감정의 특성을 반영하되 구 전체에 고르게 분포
            double adjustedInclination = adjustInclination(emotionNames[i], inclination);

            // 구면 좌표를 직교 좌표로 변환
            double x = radius * Math.sin(adjustedInclination) * Math.cos(azimuth);
            double y = radius * Math.sin(adjustedInclination) * Math.sin(azimuth);
            double z = radius * Math.cos(adjustedInclination);

            // 감정 영역 반경 설정 - 감정별 특성에 따라 조정
            double emotionRadius = calculateEmotionRadius(emotionNames[i], radius);

            emotions.add(Emotion.builder()
                    .name(emotionNames[i])
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
     * 감정 특성에 따라 수직 각도를 조정합니다.
     * 완전 균등 분포에서 약간만 조정하여 감정 특성을 반영합니다.
     */
    private double adjustInclination(String emotionName, double baseInclination) {
        // 기본 위치에서 최대 15도까지만 조정하여 전체적인 분포는 유지
        double adjustment = 0;
        double maxAdjustment = Math.PI / 12; // 15도

        switch (emotionName) {
            case "행복":
                // 행복은 약간 상단으로
                adjustment = -maxAdjustment * 0.8;
                break;
            case "희망":
                // 희망도 약간 상단으로
                adjustment = -maxAdjustment * 0.6;
                break;
            case "평화":
                // 평화는 중앙 상단으로
                adjustment = -maxAdjustment * 0.4;
                break;
            case "불안":
                // 불안은 중앙 부근으로
                adjustment = maxAdjustment * 0.1;
                break;
            case "슬픔":
                // 슬픔은 약간 하단으로
                adjustment = maxAdjustment * 0.5;
                break;
            case "분노":
                // 분노는 하단으로
                adjustment = maxAdjustment * 0.7;
                break;
            case "공포":
                // 공포는 더 하단으로
                adjustment = maxAdjustment * 0.9;
                break;
        }

        // 조정된 값이 유효 범위(0 ~ PI) 내에 있도록 보장
        return Math.max(0, Math.min(Math.PI, baseInclination + adjustment));
    }

    /**
     * 감정 특성에 따라 영역 반경을 계산합니다.
     */
    private double calculateEmotionRadius(String emotionName, double baseRadius) {
        // 기본 반경은 전체 구 반경의 40%
        double radius = baseRadius * 0.4;

        // 감정별 특성에 따라 영역 크기 조정
        switch (emotionName) {
            case "행복":
                // 행복은 약간 더 넓은 영역
                radius *= 1.1;
                break;
            case "불안":
                // 불안은 더 넓은 영역 (산만함을 표현)
                radius *= 1.2;
                break;
            case "공포":
                // 공포도 넓게 퍼진 형태
                radius *= 1.15;
                break;
            case "평화":
                // 평화는 조금 더 응집된 형태
                radius *= 0.9;
                break;
            case "희망":
                // 희망은 중간 크기
                radius *= 1.0;
                break;
            case "슬픔":
                // 슬픔은 약간 작은 영역
                radius *= 0.95;
                break;
            case "분노":
                // 분노는 집중된 영역
                radius *= 0.85;
                break;
        }

        return radius;
    }
}
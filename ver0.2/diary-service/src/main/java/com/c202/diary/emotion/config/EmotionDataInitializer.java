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

        for (int i = 0; i < totalEmotions; i++) {
            // 구면 좌표 계산
            double theta = (i * 2 * Math.PI) / totalEmotions;

            // 수직 각도 계산 - 감정별로 다른 높이에 배치
            double phi;
            String name = emotionNames[i];

            // 감정의 특성에 따라 수직 위치 조정
            if (name.equals("행복") || name.equals("희망") || name.equals("평화")) {
                // 긍정 감정은 상단에 배치
                phi = Math.PI / 4; // 45도
            } else if (name.equals("슬픔") || name.equals("분노") || name.equals("공포")) {
                // 부정 감정은 하단에 배치
                phi = 3 * Math.PI / 4; // 135도
            } else {
                // 중립 감정은 중간에 배치
                phi = Math.PI / 2; // 90도
            }

            // 구면 좌표를 직교 좌표로 변환
            double x = radius * Math.sin(phi) * Math.cos(theta);
            double y = radius * Math.sin(phi) * Math.sin(theta);
            double z = radius * Math.cos(phi);

            // 감정 영역 반경 설정 - 기본 반경의 40%로 설정
            double emotionRadius = radius * 0.4;

            // 감정별 특성에 따라 영역 반경 조정 가능
            if (name.equals("불안")) {
                emotionRadius *= 1.2; // 불안은 약간 더 넓은 영역
            } else if (name.equals("평화")) {
                emotionRadius *= 0.8; // 평화는 약간 더 좁은 영역
            }

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
}
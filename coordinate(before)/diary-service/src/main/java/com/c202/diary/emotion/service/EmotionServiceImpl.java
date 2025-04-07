package com.c202.diary.emotion.service;

import com.c202.diary.emotion.entity.Emotion;
import com.c202.diary.emotion.model.response.EmotionResponseDto;
import com.c202.diary.emotion.model.response.EmotionStatisticsResponseDto;
import com.c202.diary.emotion.repository.EmotionRepository;
import com.c202.exception.types.NotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmotionServiceImpl implements EmotionService {

    private final EmotionRepository emotionRepository;

    @Override
    public List<EmotionResponseDto> getAllEmotions() {
        List<Emotion> emotions = emotionRepository.findAll();
        return emotions.stream()
                .map(EmotionResponseDto::toDto)
                .collect(Collectors.toList());
    }

    @Override
    public EmotionResponseDto getEmotion(Integer emotionSeq) {
        Emotion emotion = emotionRepository.findByEmotionSeq(emotionSeq)
                .orElseThrow(() -> new NotFoundException("해당 감정을 찾을 수 없습니다."));
        return EmotionResponseDto.toDto(emotion);
    }

    @Override
    public EmotionResponseDto getEmotionByName(String name) {
        Emotion emotion = emotionRepository.findByName(name)
                .orElseThrow(() -> new NotFoundException("해당 감정을 찾을 수 없습니다."));
        return EmotionResponseDto.toDto(emotion);
    }

    @Override
    @Transactional
    public void incrementDiaryCount(Integer emotionSeq) {
        Emotion emotion = emotionRepository.findByEmotionSeq(emotionSeq)
                .orElseThrow(() -> new NotFoundException("해당 감정을 찾을 수 없습니다."));

        emotion = Emotion.builder()
                .emotionSeq(emotion.getEmotionSeq())
                .name(emotion.getName())
                .baseX(emotion.getBaseX())
                .baseY(emotion.getBaseY())
                .baseZ(emotion.getBaseZ())
                .baseRadius(emotion.getBaseRadius())
                .diaryCount(emotion.getDiaryCount() + 1)
                .build();

        emotionRepository.save(emotion);
    }

    @Override
    @Transactional
    public void decrementDiaryCount(Integer emotionSeq) {
        Emotion emotion = emotionRepository.findByEmotionSeq(emotionSeq)
                .orElseThrow(() -> new NotFoundException("해당 감정을 찾을 수 없습니다."));

        // 카운트가 0 이하로 내려가지 않도록 함
        int newCount = Math.max(0, emotion.getDiaryCount() - 1);

        emotion = Emotion.builder()
                .emotionSeq(emotion.getEmotionSeq())
                .name(emotion.getName())
                .baseX(emotion.getBaseX())
                .baseY(emotion.getBaseY())
                .baseZ(emotion.getBaseZ())
                .baseRadius(emotion.getBaseRadius())
                .diaryCount(newCount)
                .build();

        emotionRepository.save(emotion);
    }

    @Override
    @Transactional
    public EmotionStatisticsResponseDto getEmotionStatistics(Integer userSeq, Integer periodDays) {
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(periodDays - 1);

        String formattedStartDate = startDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        log.info("시작일{}", startDate);
        String formattedEndDate = endDate.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        log.info("종료일{}", endDate);

        List<Map<String, Object>> statistics =
                emotionRepository.getEmotionStatisticsByPeriod(userSeq, formattedStartDate, formattedEndDate);

        List<EmotionStatisticsResponseDto.EmotionCount> emotionCounts = statistics.stream()
                .map(row -> {
                    String emotion = (String) row.get("emotion");
                    Long count = ((Number) row.get("count")).longValue();
                    return EmotionStatisticsResponseDto.EmotionCount.builder()
                            .emotion(emotion)
                            .count(count)
                            .build();
                })
                .collect(Collectors.toList());

        return EmotionStatisticsResponseDto.builder()
                .periodDays(periodDays)
                .data(emotionCounts)
                .build();
    }

}
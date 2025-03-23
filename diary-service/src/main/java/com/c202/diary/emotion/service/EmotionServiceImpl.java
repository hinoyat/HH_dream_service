package com.c202.diary.emotion.service;

import com.c202.diary.emotion.entity.Emotion;
import com.c202.diary.emotion.model.response.EmotionResponseDto;
import com.c202.diary.emotion.repository.EmotionRepository;
import com.c202.exception.CustomException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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
                .orElseThrow(() -> new CustomException("해당 감정을 찾을 수 없습니다."));
        return EmotionResponseDto.toDto(emotion);
    }

    @Override
    public EmotionResponseDto getEmotionByName(String name) {
        Emotion emotion = emotionRepository.findByName(name)
                .orElseThrow(() -> new CustomException("해당 감정을 찾을 수 없습니다."));
        return EmotionResponseDto.toDto(emotion);
    }

    @Override
    @Transactional
    public void incrementDiaryCount(Integer emotionSeq) {
        Emotion emotion = emotionRepository.findByEmotionSeq(emotionSeq)
                .orElseThrow(() -> new CustomException("해당 감정을 찾을 수 없습니다."));

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
                .orElseThrow(() -> new CustomException("해당 감정을 찾을 수 없습니다."));

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
}
package com.c202.diary.emotion.repository;

import com.c202.diary.emotion.entity.Emotion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmotionRepository extends JpaRepository<Emotion, Integer> {

    Optional<Emotion> findByName(String name);
    Optional<Emotion> findByEmotionSeq(Integer emotionSeq);
    List<Emotion> findAll();
}

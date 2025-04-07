package com.c202.diary.emotion.repository;

import com.c202.diary.emotion.entity.Emotion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Map;
import java.util.Optional;

public interface EmotionRepository extends JpaRepository<Emotion, Integer> {

    Optional<Emotion> findByName(String name);
    Optional<Emotion> findByEmotionSeq(Integer emotionSeq);
    List<Emotion> findAll();

    @Query(value = "SELECT e.name as emotion, COUNT(d.diarySeq) as count " +
            "FROM emotion e LEFT JOIN diary d ON d.emotionSeq = e.emotionSeq " +
            "AND d.userSeq = :userSeq " +
            "AND d.isDeleted = 'N' " +
            "AND d.dreamDate BETWEEN :startDate AND :endDate " +
            "GROUP BY e.name", nativeQuery = true)
    List<Map<String, Object>> getEmotionStatisticsByPeriod(
            @Param("userSeq") Integer userSeq,
            @Param("startDate") String startDate,
            @Param("endDate") String endDate);
}

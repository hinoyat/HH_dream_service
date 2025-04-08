package com.c202.diary.like.repository;

import com.c202.diary.like.entity.DiaryLike;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DiaryLikeRepository extends JpaRepository<DiaryLike, Integer> {

    Integer countByDiarySeq(Integer diarySeq);

    // 특정 사용자가 특정 일기에 좋아요를 눌렀는지 확인
    Optional<DiaryLike> findByDiarySeqAndUserSeq(Integer diarySeq, Integer userSeq);

    // 특정 사용자가 좋아요를 누른 일기 목록 조회
    List<DiaryLike> findByUserSeq(Integer userSeq);

    // 특정 일기의 좋아요 목록 조회
    List<DiaryLike> findByDiarySeq(Integer diarySeq);
}

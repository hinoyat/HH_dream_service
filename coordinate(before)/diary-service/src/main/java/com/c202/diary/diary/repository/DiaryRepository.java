package com.c202.diary.diary.repository;

import com.c202.diary.diary.entity.Diary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DiaryRepository extends JpaRepository<Diary, Integer> {

    Optional<Diary> findByDiarySeq(Integer diarySeq);

    Optional<Diary> findByDiarySeqAndIsDeleted(Integer diarySeq, String isDeleted);

    List<Diary> findByUserSeqAndIsDeleted(Integer userSeq, String isDeleted);

    List<Diary> findByUserSeqAndIsPublicAndIsDeleted(Integer userSeq, String isPublic, String isDeleted);

    List<Diary> findByIsDeleted(String isDeleted);
}

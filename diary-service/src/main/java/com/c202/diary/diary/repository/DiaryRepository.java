package com.c202.diary.diary.repository;

import com.c202.diary.diary.entity.Diary;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DiaryRepository extends JpaRepository<Diary, Integer> {

    Diary findByDiarySeq(int diarySeq);


    List<Diary> findByUserSeq(Long userSeq);
}

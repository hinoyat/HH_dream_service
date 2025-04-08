package com.c202.diary.tag.repository;

import com.c202.diary.diary.entity.Diary;
import com.c202.diary.tag.entity.DiaryTag;
import com.c202.diary.tag.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DiaryTagRepository extends JpaRepository<DiaryTag, Integer> {
    @Query(value = "SELECT t.* FROM tag t " +
            "JOIN diarytag dt ON t.tagSeq = dt.tagSeq " +
            "JOIN diary d ON d.diarySeq = dt.diarySeq " +
            "WHERE d.userSeq = :userSeq " +
            "GROUP BY t.tagSeq " +
            "ORDER BY MAX(dt.createdAt) DESC " +
            "LIMIT :limit", nativeQuery = true)
    List<Tag> findRecentTagsByUserSeq(Integer userSeq, Integer limit);  // 해당 유저의 최근 7개 등록 태그 조회

    void deleteByDiary(Diary diary);

    List<DiaryTag> findByDiary(Diary diary);
}

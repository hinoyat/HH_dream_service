package com.c202.diary.tag.repository;

import com.c202.diary.tag.entity.DiaryTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DiaryTagRepository extends JpaRepository<DiaryTag, Integer> {

}

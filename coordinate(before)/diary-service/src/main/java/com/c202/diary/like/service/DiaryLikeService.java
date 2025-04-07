package com.c202.diary.like.service;

public interface DiaryLikeService {

    String toggleLike(Integer diarySeq, Integer userSeq);

    Integer getLikeCount(Integer diarySeq);

    boolean hasUserLiked(Integer diarySeq, Integer userSeq);
}

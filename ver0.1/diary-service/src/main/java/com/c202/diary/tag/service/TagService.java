package com.c202.diary.tag.service;

import com.c202.diary.diary.entity.Diary;
import com.c202.diary.tag.entity.Tag;
import com.c202.diary.tag.model.response.TagResponseDto;

import java.util.List;

public interface TagService {

    List<TagResponseDto> getRecentTags(Integer userSeq, Integer limit);

    Tag createTagIfNotExists(String tagName);

    List<TagResponseDto> processTags(Diary diary, List<String> tagNames, String timestamp);
}
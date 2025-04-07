package com.c202.diary.tag.service;

import com.c202.diary.diary.entity.Diary;
import com.c202.diary.tag.entity.DiaryTag;
import com.c202.diary.tag.entity.Tag;
import com.c202.diary.tag.model.response.TagResponseDto;
import com.c202.diary.tag.repository.DiaryTagRepository;
import com.c202.diary.tag.repository.TagRepository;
import com.c202.exception.types.ValidationException;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TagServiceImpl implements TagService {

    private final TagRepository tagRepository;
    private final DiaryTagRepository diaryTagRepository;

    @Override
    public List<TagResponseDto> getRecentTags(Integer userSeq, Integer limit) {

        List<Tag> recentTags = diaryTagRepository.findRecentTagsByUserSeq(userSeq, limit);

        return recentTags.stream()
                .map(TagResponseDto::toDto)
                .collect(Collectors.toList());
    }



    @Override
    @Transactional
    public List<TagResponseDto> processTags(Diary diary, List<String> tagNames, String now) {

        if (tagNames == null || tagNames.isEmpty()) {
            return new ArrayList<>();
        }

        if (tagNames.size() > 3) {
            throw new ValidationException("태그는 3개까지 가능합니다");
        }

        List<TagResponseDto> tagDtos = new ArrayList<>();
        for (String tagName : tagNames) {

            validateTagName(tagName);

            Tag tag = createTagIfNotExists(tagName);

            DiaryTag diaryTag = DiaryTag.builder()
                    .diary(diary)
                    .tag(tag)
                    .createdAt(now)
                    .updatedAt(now)
                    .build();
            diaryTagRepository.save(diaryTag);

            tagDtos.add(TagResponseDto.toDto(tag));
        }

        return tagDtos;
    }


    @Override
    @Transactional
    public Tag createTagIfNotExists(String tagName) {
        return tagRepository.findByName(tagName)
                .orElseGet(() -> tagRepository.save(Tag.builder()
                        .name(tagName)
                        .build()));
    }

    private void validateTagName(String tagName) {
        if (tagName.length() > 5) {
            throw new ValidationException("태그는 5글자까지 가능합니다");
        }
        if (!tagName.matches("^[가-힣a-zA-Z0-9]+$")) {
            throw new ValidationException("태그는 한글, 영문, 숫자만 사용 가능합니다");
        }
    }

}
package com.c202.diary.diary.model.request;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class DiaryCreateRequestDto {

    private String title;

    private String content;

    private String dreamDate;

    private String isPublic;

    private List<String> tags;

    private String mainEmotion;

}

package com.c202.diary.diary.model.request;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class DiaryCreateRequestDto {

    private String title;

    private String content;

    private String dreamDate;

    private String isPublic;

}

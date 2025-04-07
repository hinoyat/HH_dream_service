package com.c202.diary.diary.model.request;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class VideoRequestDto {
    private String videoUrl;
}

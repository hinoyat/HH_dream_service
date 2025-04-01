package com.c202.diary.elastic.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiarySearchRequestDto {
    private String keyword;
    private boolean searchTitle = true;
    private boolean searchContent = true;
    private boolean searchTag = true;
    private boolean currentUserOnly = false;
}
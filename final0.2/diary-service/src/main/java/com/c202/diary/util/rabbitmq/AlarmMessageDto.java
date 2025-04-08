package com.c202.diary.util.rabbitmq;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AlarmMessageDto {
    private Integer userSeq;
    private String content;
    private String type;
    private Integer diarySeq;
    private boolean requiresPersistence;
}
package com.c202.diary.tag.model.response;

import com.c202.diary.tag.entity.Tag;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TagResponseDto {
    private Integer tagSeq;
    private String name;

    public static TagResponseDto toDto(Tag tag) {
        return TagResponseDto.builder()
                .tagSeq(tag.getTagSeq())
                .name(tag.getName())
                .build();
    }
}
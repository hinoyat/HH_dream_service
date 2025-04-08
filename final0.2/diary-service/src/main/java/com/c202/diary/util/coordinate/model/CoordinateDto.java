package com.c202.diary.util.coordinate.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoordinateDto {
    private Double x;
    private Double y;
    private Double z;
    private Integer emotionSeq;
    private String emotionName;
    private Double distance;
}
package com.c202.diary.util.coordinate.model;

import com.c202.diary.diary.entity.Diary;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClusterDto {
    private List<Diary> diaries = new ArrayList<>();
    private double centerX;
    private double centerY;
    private double centerZ;
    private double similarityToNewDiary;

}
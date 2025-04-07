package com.c202.diary.emotion.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="emotion")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Emotion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer emotionSeq;

    @Column(nullable = false, length = 50, unique = true)
    private String name;

    @Column(nullable = false)
    private Double baseX;

    @Column(nullable = false)
    private Double baseY;

    @Column(nullable = false)
    private Double baseZ;

    @Column(nullable = false)
    private Double baseRadius;

    @Column(nullable = false)
    private Integer diaryCount;
}

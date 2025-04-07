package com.c202.diary.like.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name="diarylike")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiaryLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer likeSeq;

    @Column(nullable = false)
    private Integer diarySeq;

    @Column(nullable = false)
    private Integer userSeq;

    @Column(nullable = false, length = 15)
    private String createdAt;
}

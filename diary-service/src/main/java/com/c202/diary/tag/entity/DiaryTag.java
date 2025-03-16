package com.c202.diary.tag.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiaryTag {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long diaryTagSeq;

    @Column(nullable = false)
    private String diarySeq;

    @Column(nullable = false)
    private String tagSeq;

    @Column(nullable = false, length = 15)
    private String createdAt;

    @Column(nullable = false, length = 15)
    private String updatedAt;
}

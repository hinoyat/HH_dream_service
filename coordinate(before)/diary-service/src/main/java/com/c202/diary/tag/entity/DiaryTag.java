package com.c202.diary.tag.entity;

import com.c202.diary.diary.entity.Diary;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name="diarytag")
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DiaryTag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer diaryTagSeq;


    @ManyToOne(fetch = FetchType.LAZY)  // Diary와 매핑
    @JoinColumn(name = "diarySeq")
    private Diary diary;

    @ManyToOne(fetch = FetchType.LAZY)  // Tag와 매핑
    @JoinColumn(name = "tagSeq")
    private Tag tag;

    @Column(nullable = false, length = 15)
    private String createdAt;

    @Column(nullable = false, length = 15)
    private String updatedAt;


}

package com.c202.diary.diary.entity;

import com.c202.diary.tag.entity.DiaryTag;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Diary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer diarySeq;

    @Column(nullable = false)
    private Integer userSeq;

    @Column(nullable = false, length = 50)
    private String title;

    @Column(nullable = false, length = 255)
    private String content;

    @Column(nullable = true, length = 100)
    private String videoUrl;

    @Column(nullable = false, length = 8)
    private String dreamDate;

    @Column(nullable = false, length = 15)
    private String createdAt;

    @Column(nullable = false, length = 15)
    private String updatedAt;

    @Column(nullable = true, length = 15)
    private String deletedAt;

    @Column(nullable = false, length = 1)
    private String isDeleted;

    @Column(nullable = false, length = 1)
    private String isPublic;

    @OneToMany(mappedBy = "diary", cascade = CascadeType.ALL, orphanRemoval = true) // DiaryTag와 연결
    @Builder.Default
    private List<DiaryTag> diaryTags = new ArrayList<>();
    
    public void deleteDiary() { this.isDeleted = "Y"; }

    public void setPublic(String isPublic) { this.isPublic = isPublic; }

    public void update(String title, String content, String dreamDate, String updatedAt) {
        this.title = title;
        this.content = content;
        this.dreamDate = dreamDate;
        this.updatedAt = updatedAt;
    }
}

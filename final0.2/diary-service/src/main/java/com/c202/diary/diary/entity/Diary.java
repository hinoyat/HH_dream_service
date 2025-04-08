package com.c202.diary.diary.entity;

import com.c202.diary.tag.entity.DiaryTag;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "diary")
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

    @Column(nullable = true, length = 255)
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

    @Column(nullable = true)
    private Double x;

    @Column(nullable = true)
    private Double y;

    @Column(nullable = true)
    private Double z;

    @Column(nullable = true)
    private Integer emotionSeq;

    @OneToMany(mappedBy = "diary", cascade = CascadeType.ALL, orphanRemoval = true) // DiaryTag와 연결
    @Builder.Default
    private List<DiaryTag> diaryTags = new ArrayList<>();
    
    public void deleteDiary() { this.isDeleted = "Y"; }

    public void setPublic(String isPublic) { this.isPublic = isPublic; }

    public void update(String title, String content, String isPublic, String dreamDate, String updatedAt) {
        this.title = title;
        this.content = content;
        this.isPublic = isPublic;
        this.dreamDate = dreamDate;
        this.updatedAt = updatedAt;
    }

    public void setCoordinates(Double x, Double y, Double z, Integer emotionSeq) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.emotionSeq = emotionSeq;
    }
    public void setCoordinate(Double x, Double y, Double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public void setVideo(String videoUrl) { this.videoUrl = videoUrl; }
}

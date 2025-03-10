package com.c202.diaryservice.domain.entity;

import jakarta.persistence.*;

@Entity
public class Diary {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int diarySeq;

    @Column
    private String Title;

    @Column
    private String content;

    @Column
    private String videoUrl;

    @Column
    private String dreamDate;

    @Column
    private String createdAt;

    @Column
    private String updatedAt;

    @Column
    private String deletedAt;

    @Column
    private String isDeleted;

    @Column
    private String isPublic;

}

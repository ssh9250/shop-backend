package com.study.shop.domain.post.entity;

import jakarta.persistence.*;

@Entity
public class PostFile {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String originalFileName;
    private String storedFileName;
    private String filePath;
    private Long fileSize;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private Post post;
}

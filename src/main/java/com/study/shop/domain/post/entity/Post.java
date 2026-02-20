package com.study.shop.domain.post.entity;

import com.study.shop.domain.comment.entity.Comment;
import com.study.shop.domain.member.entity.Member;
import com.study.shop.global.util.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Post extends BaseTimeEntity {
    @Id
    @GeneratedValue
    private Long id;

    private String title;

    @Column(columnDefinition = "TEXT")
    private String content;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Comment> comments = new ArrayList<>();

    // todo: 단방향으로 연결할때에는 어떻게 할 지 생각해보기
    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PostFile> postFiles = new ArrayList<>();

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    public void addComment(Comment comment) {
        if (comment != null && !this.comments.contains(comment)) {
            this.comments.add(comment);
            comment.setPost(this);
            //  캡슐화를 위해 assign 프라이빗 메소드로 작성
            //  개발 스타일 변화를 확인하기 위해 일부러 남겨놓음
        }
    }

    //  댓글 삭제 시 연관관계는 끊어지지만, 내가 작성한 댓글 조회 -> 모두 보임
    //  대안 : orphanRemoval, 소프트 삭제
    public void removeComment(Comment comment) {
        this.comments.remove(comment);
        comment.setPost(null);
    }
}

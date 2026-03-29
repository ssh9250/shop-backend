package com.study.shop.domain.comment.entity;

import com.study.shop.domain.post.entity.Post;
import com.study.shop.domain.member.entity.Member;
import com.study.shop.global.util.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@SQLDelete(sql = "update comment set deleted_at = NOW() where id = ?")
@SQLRestriction("deleted_at is NULL")
public class Comment extends BaseTimeEntity {
    @Id
    @GeneratedValue
    private Long id;

    @Column(nullable = false)
    private String writer;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Setter // Post 연관관계 편의 메서드 용도
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    void assignPost(Post post) {
        this.post = post;
        post.getComments().add(this);
    }
    void assignMember(Member member) {
        this.member = member;
        member.getComments().add(this);
    }

    public static Comment create(Member member, Post post, String content) {
        Comment comment = Comment.builder()
                .writer(member.getEmail())
                .content(content)
                .build();

        comment.assignPost(post);
        comment.assignMember(member);

        return comment;
    }
    public void update(String content) {
        this.content = content;
    }
}

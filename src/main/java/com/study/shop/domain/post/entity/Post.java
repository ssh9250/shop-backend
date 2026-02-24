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

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<PostFile> postFiles = new ArrayList<>();

    @Setter
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    void assignMember(Member member) {
        this.member = member;
        member.getPosts().add(this);
    }

    public static Post create(String title, String content, Member member) {
        Post post = Post.builder().title(title).content(content).build();
        post.assignMember(member);
        return post;
    }

    public void update(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public void addPostFile(PostFile postFile) {
        postFiles.add(postFile);
        postFile.assignPost(this);
    }

    // 행위의 주체가 되는 엔티티(comment)에서 실행하는 것이 좋으므로 현재는 안쓰는 로직
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

    // 어차피 updatePost 시 변경감지를 위해 clear 사용으로 안 쓰여질 것 같음
    public void removePostFile(PostFile postFile) {
        this.postFiles.remove(postFile);
        postFile.assignPost(null);
    }
}

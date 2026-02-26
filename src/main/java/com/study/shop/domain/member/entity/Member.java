package com.study.shop.domain.member.entity;

import com.study.shop.domain.Item.entity.Item;
import com.study.shop.domain.order.entity.Order;
import com.study.shop.domain.post.entity.Post;
import com.study.shop.domain.comment.entity.Comment;
import com.study.shop.global.enums.RoleType;
import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Member {
    @Id
    @GeneratedValue
    private Long id;

    @Email
    @NotBlank
    @Column(nullable = false, unique = true, length = 50)
    private String email;

    @Column(nullable = false, length = 200) // password Encrypt 고려
    private String password;

    @Column(nullable = false, length = 50)
    private String nickname;

    @Column(length = 50)
    private String phone;

    private String address;

    @Enumerated(EnumType.STRING)
    private RoleType role;

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Post> posts = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Comment> comments = new ArrayList<>();

    //  todo: 만약 다른 사람과의 거래 내역에서 한쪽이 탈퇴한다면?
    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Order> orders = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Item> items = new ArrayList<>();

    public void updateProfile(String nickname, String phone, String address) {
        this.nickname = nickname;
        this.phone = phone;
        this.address = address;
    }

    public void updatePassword(String newPassword) {
        this.password = newPassword;
    }



    // 행위의 주체가 되는 post, comment 쪽에서 연관관계 편의 메서드로 양방향 동기화
    public void addPost(Post post) {
        if (post != null && !this.posts.contains(post)) {
            this.posts.add(post);
            post.setMember(this);
        }
    }

    public void addComment(Comment comment) {
        if (comment != null && !this.comments.contains(comment)) {
            this.comments.add(comment);
            comment.setMember(this);
        }
    }

    // orphan removal, cascade로 필요없음

    public void removePost(Post post) {
        this.posts.remove(post);
        post.setMember(null);
    }

    public void removeComment(Comment comment) {
        this.comments.remove(comment);
        comment.setMember(null);
    }
}
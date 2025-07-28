package com.study.shop.testutil;

import com.study.shop.domain.board.entity.Post;
import com.study.shop.domain.comment.entity.Comment;

public class CommentTestHelper {
    public static Comment createComment(Post post, String writer, String content) {
        return Comment.builder()
                .post(post)
                .writer(writer)
                .content(content)
                .build();
    }
}

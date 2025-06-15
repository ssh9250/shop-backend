package com.study.cruisin.testutil;

import com.study.cruisin.domain.board.entity.Post;
import com.study.cruisin.domain.comment.entity.Comment;

public class CommentTestHelper {
    public static Comment createComment(Post post, String writer, String content) {
        return Comment.builder()
                .post(post)
                .writer(writer)
                .content(content)
                .build();
    }
}

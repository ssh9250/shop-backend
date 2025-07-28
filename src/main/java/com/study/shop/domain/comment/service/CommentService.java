package com.study.shop.domain.comment.service;

import com.study.shop.domain.board.entity.Post;
import com.study.shop.domain.board.exception.PostNotFoundException;
import com.study.shop.domain.board.repository.PostRepository;
import com.study.shop.domain.comment.dto.CommentResponseDto;
import com.study.shop.domain.comment.dto.CreateCommentRequestDto;
import com.study.shop.domain.comment.dto.UpdateCommentRequestDto;
import com.study.shop.domain.comment.entity.Comment;
import com.study.shop.domain.comment.exception.CommentNotFoundException;
import com.study.shop.domain.comment.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CommentService {
    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    public Long createComment(CreateCommentRequestDto requestDto) {
        Post post = postRepository.findById(requestDto.getPostId())
                .orElseThrow(() -> new PostNotFoundException(requestDto.getPostId()));

        Comment comment = Comment.builder()
                .writer(requestDto.getWriter())
                .content(requestDto.getContent())
                .post(post)
                .build();

        post.addComment(comment);
        return commentRepository.save(comment).getId();
    }

    @Transactional(readOnly = true)
    public List<CommentResponseDto> getCommentsByPostId(Long postId) {
        return commentRepository.findByPostId(postId).stream()
                .map(CommentResponseDto::from)
                .collect(Collectors.toList());
    }

    public void updateComment(Long id, UpdateCommentRequestDto requestDto) {
        Comment comment = commentRepository.findById(id)
                .orElseThrow(() -> new CommentNotFoundException(id));
        comment.update(requestDto.getContent());
    }

    public void deleteComment(Long id) {
        Comment comment = commentRepository.findById(id)
                        .orElseThrow(() -> new CommentNotFoundException(id));
        Post post = comment.getPost();
        post.removeComment(comment);
        commentRepository.delete(comment);
    }
}

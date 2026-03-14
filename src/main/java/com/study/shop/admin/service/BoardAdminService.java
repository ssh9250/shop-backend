package com.study.shop.admin.service;

import com.study.shop.admin.dto.AdminCommentResponseDto;
import com.study.shop.domain.comment.entity.Comment;
import com.study.shop.domain.comment.exception.CommentNotFoundException;
import com.study.shop.domain.comment.repository.CommentRepository;
import com.study.shop.domain.post.dto.PostDetailDto;
import com.study.shop.domain.post.entity.Post;
import com.study.shop.domain.post.exception.PostNotFoundException;
import com.study.shop.domain.post.repository.PostRepository;
import com.study.shop.domain.post.service.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class BoardAdminService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final FileStorageService fileStorageService;

    // ==================== 게시글 ====================

    @Transactional(readOnly = true)
    public List<PostDetailDto> getAllPosts() {
        return postRepository.findAll().stream()
                .map(PostDetailDto::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PostDetailDto getPostById(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));
        return PostDetailDto.from(post);
    }

    @Transactional(readOnly = true)
    public List<PostDetailDto> getPostsByMemberId(Long memberId) {
        return postRepository.findByMemberId(memberId).stream()
                .map(PostDetailDto::from)
                .collect(Collectors.toList());
    }

    public void deletePost(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));

        post.getPostFiles().forEach(file -> fileStorageService.deleteFile(file.getStoredFileName()));
        postRepository.delete(post);
    }

    // ==================== 댓글 ====================

    @Transactional(readOnly = true)
    public List<AdminCommentResponseDto> getAllComments() {
        return commentRepository.findAll().stream()
                .map(AdminCommentResponseDto::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<AdminCommentResponseDto> getCommentsByPostId(Long postId) {
        return commentRepository.findAllByPostId(postId).stream()
                .map(AdminCommentResponseDto::from)
                .collect(Collectors.toList());
    }

    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));
        commentRepository.delete(comment);
    }
}

package com.study.shop.domain.comment.service;

import com.study.shop.domain.member.entity.Member;
import com.study.shop.domain.member.exception.MemberNotFoundException;
import com.study.shop.domain.member.repository.MemberRepository;
import com.study.shop.domain.post.entity.Post;
import com.study.shop.domain.post.exception.PostNotFoundException;
import com.study.shop.domain.post.repository.PostRepository;
import com.study.shop.domain.comment.dto.CommentResponseDto;
import com.study.shop.domain.comment.dto.CreateCommentRequestDto;
import com.study.shop.domain.comment.dto.UpdateCommentRequestDto;
import com.study.shop.domain.comment.entity.Comment;
import com.study.shop.domain.comment.exception.CommentNotFoundException;
import com.study.shop.domain.comment.repository.CommentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
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
    private final MemberRepository memberRepository;

    public Long createComment(Long memberId, CreateCommentRequestDto requestDto) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));
        Post post = postRepository.findById(requestDto.getPostId())
                .orElseThrow(() -> new PostNotFoundException(requestDto.getPostId()));

        Comment comment = Comment.create(member, post, requestDto.getContent());

        return commentRepository.save(comment).getId();
    }

    @Transactional(readOnly = true)
    public List<CommentResponseDto> getCommentsByPostId(Long postId) {
        return commentRepository.findByPostId(postId).stream()
                .map(CommentResponseDto::from)
                .collect(Collectors.toList());
    }

    public void updateComment(Long memberId, Long commentId, UpdateCommentRequestDto requestDto) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CommentNotFoundException(commentId));

        validateCommentAccess(memberId, comment);

        comment.update(requestDto.getContent());
    }

    public void deleteComment(Long memberId, Long commentId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));
        Comment comment = commentRepository.findById(commentId)
                        .orElseThrow(() -> new CommentNotFoundException(commentId));

        validateCommentAccess(memberId, comment);

        commentRepository.delete(comment);
    }

    public void validateCommentAccess(Long memberId, Comment comment) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));
        if (!memberId.equals(comment.getMember().getId())) {
            throw new AccessDeniedException("해당 작업을 수행할 권한이 없습니다.");
        }
    }
}

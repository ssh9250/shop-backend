package com.study.cruisin.domain.comment.controller;

import com.study.cruisin.domain.comment.dto.CommentResponseDto;
import com.study.cruisin.domain.comment.dto.CreateCommentRequestDto;
import com.study.cruisin.domain.comment.dto.UpdateCommentRequestDto;
import com.study.cruisin.domain.comment.service.CommentService;
import com.study.cruisin.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comments")
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<ApiResponse<Long>> createComment(@RequestBody @Valid CreateCommentRequestDto request) {
        Long commentId = commentService.createComment(request);
        return ResponseEntity.ok(ApiResponse.success(commentId));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<CommentResponseDto>>> getCommentsByPostId(@RequestParam Long postId) {
        List<CommentResponseDto> comments = commentService.getCommentsByPostId(postId);
        return ResponseEntity.ok(ApiResponse.success(comments));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> updateComment(@PathVariable Long id, @RequestBody @Valid UpdateCommentRequestDto requestDto) {
        commentService.updateComment(id, requestDto);
        return ResponseEntity.ok(ApiResponse.success(null, "댓글이 수정되었습니다."));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(@PathVariable Long id) {
        commentService.deleteComment(id);
        return ResponseEntity.ok(ApiResponse.success(null, "댓글이 삭제되었습니다."));
    }
}
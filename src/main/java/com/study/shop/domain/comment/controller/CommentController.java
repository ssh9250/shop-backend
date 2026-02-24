package com.study.shop.domain.comment.controller;

import com.study.shop.domain.comment.dto.CommentResponseDto;
import com.study.shop.domain.comment.dto.CreateCommentRequestDto;
import com.study.shop.domain.comment.dto.UpdateCommentRequestDto;
import com.study.shop.domain.comment.service.CommentService;
import com.study.shop.global.response.ApiResponse;
import com.study.shop.security.auth.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/comments")
@Tag(name = "Comment", description = "댓글 관련 API")
public class CommentController {

    private final CommentService commentService;

    @Operation(summary = "댓글 작성", description = "새로운 댓글을 작성합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<Long>> createComment(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                           @RequestBody @Valid CreateCommentRequestDto request) {
        Long commentId = commentService.createComment(userDetails.getMemberId(), request);
        return ResponseEntity.ok(ApiResponse.success(commentId));
    }

    @Operation(summary = "게시글 댓글 조회", description = "게시글 id로 해당 게시글에 달린 모든 댓글들을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<CommentResponseDto>>> getCommentsByPostId(@RequestParam Long postId) {
        List<CommentResponseDto> comments = commentService.getCommentsByPostId(postId);
        return ResponseEntity.ok(ApiResponse.success(comments));
    }

    @Operation(summary = "댓글 수정", description = "id로 특정 댓글을 수정합니다.")
    @PatchMapping("/{commentId}")
    public ResponseEntity<ApiResponse<Void>> updateComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long commentId,
            @RequestBody @Valid UpdateCommentRequestDto requestDto) {
        commentService.updateComment(userDetails.getMemberId(), commentId, requestDto);
        return ResponseEntity.ok(ApiResponse.success(null, "댓글이 수정되었습니다."));
    }

    @Operation(summary = "댓글 삭제", description = "id로 특정 댓글을 삭제합니다.")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id) {
        commentService.deleteComment(userDetails.getMemberId(), id);
        return ResponseEntity.ok(ApiResponse.success(null, "댓글이 삭제되었습니다."));
    }
}
package com.study.shop.admin.controller;

import com.study.shop.admin.dto.AdminCommentResponseDto;
import com.study.shop.admin.service.BoardAdminService;
import com.study.shop.domain.post.dto.PostResponseDto;
import com.study.shop.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/board")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Board Admin", description = "관리자 게시판 관리 API (게시글 + 댓글)")
public class BoardAdminController {
    //  todo: 관리자 로직 세부적으로 좀 더 추가

    private final BoardAdminService boardAdminService;

    // ==================== 게시글 ====================

    @Operation(summary = "전체 게시글 조회", description = "모든 게시글을 조회합니다.")
    @GetMapping("/posts")
    public ResponseEntity<ApiResponse<List<PostResponseDto>>> getAllPosts() {
        return ResponseEntity.ok(ApiResponse.success(boardAdminService.getAllPosts()));
    }

    @Operation(summary = "게시글 단건 조회", description = "id로 게시글을 조회합니다.")
    @GetMapping("/posts/{postId}")
    public ResponseEntity<ApiResponse<PostResponseDto>> getPostById(@PathVariable Long postId) {
        return ResponseEntity.ok(ApiResponse.success(boardAdminService.getPostById(postId)));
    }

    @Operation(summary = "회원별 게시글 조회", description = "특정 회원이 작성한 게시글 목록을 조회합니다.")
    @GetMapping("/posts/member/{memberId}")
    public ResponseEntity<ApiResponse<List<PostResponseDto>>> getPostsByMemberId(@PathVariable Long memberId) {
        return ResponseEntity.ok(ApiResponse.success(boardAdminService.getPostsByMemberId(memberId)));
    }

    @Operation(summary = "게시글 강제 삭제", description = "게시글과 첨부 파일을 강제 삭제합니다.")
    @DeleteMapping("/posts/{postId}")
    public ResponseEntity<ApiResponse<Void>> deletePost(@PathVariable Long postId) {
        boardAdminService.deletePost(postId);
        return ResponseEntity.ok(ApiResponse.success(null, "게시글이 삭제되었습니다."));
    }

    // ==================== 댓글 ====================

    @Operation(summary = "전체 댓글 조회", description = "소프트 삭제된 댓글을 포함한 모든 댓글을 조회합니다.")
    @GetMapping("/comments")
    public ResponseEntity<ApiResponse<List<AdminCommentResponseDto>>> getAllComments() {
        return ResponseEntity.ok(ApiResponse.success(boardAdminService.getAllComments()));
    }

    @Operation(summary = "게시글별 댓글 조회", description = "특정 게시글의 댓글을 소프트 삭제된 댓글을 포함하여 조회합니다.")
    @GetMapping("/comments/post/{postId}")
    public ResponseEntity<ApiResponse<List<AdminCommentResponseDto>>> getCommentsByPostId(@PathVariable Long postId) {
        return ResponseEntity.ok(ApiResponse.success(boardAdminService.getCommentsByPostId(postId)));
    }

    @Operation(summary = "댓글 강제 삭제", description = "댓글을 강제로 완전 삭제합니다.")
    @DeleteMapping("/comments/{commentId}")
    public ResponseEntity<ApiResponse<Void>> deleteComment(@PathVariable Long commentId) {
        boardAdminService.deleteComment(commentId);
        return ResponseEntity.ok(ApiResponse.success(null, "댓글이 삭제되었습니다."));
    }
}

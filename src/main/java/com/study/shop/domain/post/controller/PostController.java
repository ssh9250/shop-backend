package com.study.shop.domain.post.controller;

import com.study.shop.domain.post.dto.CreatePostRequestDto;
import com.study.shop.domain.post.dto.PostResponseDto;
import com.study.shop.domain.post.dto.UpdatePostRequestDto;
import com.study.shop.domain.post.service.PostService;
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
@RequestMapping("/api/posts")
@Tag(name = "Post", description = "게시글 관련 API")
public class PostController {
    private final PostService postService;

    @Operation(summary = "게시글 작성", description = "게시글을 생성합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<Long>> createPost(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody @Valid CreatePostRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.success(postService.createPost(userDetails.getMemberId(), requestDto)));
    }

    @Operation(summary = "전체 게시글 조회", description = "모든 게시글을 조회합니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<PostResponseDto>>> getAllPosts() {
        return ResponseEntity.ok(ApiResponse.success(postService.getAllPosts()));
    }

    @Operation(summary = "게시글 단건 조회", description = "id를 통해 특정 게시글을 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PostResponseDto>> getPost(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(postService.getPostById(id)));
    }

    @Operation(summary = "게시글 수정", description = "id를 통해 특정 게시글을 수정합니다.")
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> updatePost(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long id, @RequestBody @Valid UpdatePostRequestDto requestDto) {
        postService.updatePost(userDetails.getMemberId(), id, requestDto);
        return ResponseEntity.ok(ApiResponse.success(null, "게시글이 수정되었습니다."));
    }

    @Operation(summary = "게시글 삭제", description = "게시글을 삭제합니다.")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePost(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long id) {
        postService.deletePost(userDetails.getMemberId(), id);
        return ResponseEntity.ok(ApiResponse.success(null, "게시글이 삭제되었습니다."));
    }
}

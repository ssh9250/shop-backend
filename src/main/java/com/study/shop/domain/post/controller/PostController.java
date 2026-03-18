package com.study.shop.domain.post.controller;

import com.study.shop.domain.post.dto.*;
import com.study.shop.domain.post.service.PostService;
import com.study.shop.global.response.ApiResponse;
import com.study.shop.security.auth.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
@Tag(name = "Post", description = "게시글 관련 API")
public class PostController {
    private final PostService postService;

    @Operation(summary = "게시글 작성", description = "게시글을 생성합니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<Long>> createPost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestPart("request") @Valid CreatePostRequestDto requestDto,
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) {
        return ResponseEntity.ok(ApiResponse.success(postService.createPost(userDetails.getMemberId(), requestDto, files)));
    }

    // 요청 예시 : GET /api/posts?page=0&size=20&sort=createdAt,desc&title=foo&writer=bar&from=2024-01-01T00:00:00&to=2024-12-31T23:59:59
    @Operation(summary = "게시글 목록 조회", description = "검색 조건에 맞는 게시글을 조회합니다. 조건 없이 호출하면 전체 조회입니다.")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<PostListDto>>> searchPosts(
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @ModelAttribute PostSearchConditionDto cond
    ) {
        return ResponseEntity.ok(ApiResponse.success(postService.searchPosts(pageable, cond)));
    }

    @Operation(summary = "게시글 단건 조회", description = "id를 통해 특정 게시글을 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PostDetailDto>> getPost(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(postService.getPostById(id)));
    }

    @Operation(summary = "게시글 수정", description = "id를 통해 특정 게시글을 수정합니다.")
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> updatePost(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Long id,
            @RequestPart("request") @Valid UpdatePostRequestDto requestDto,
            @RequestPart(value = "files", required = false) List<MultipartFile> files) {
        postService.updatePost(userDetails.getMemberId(), id, requestDto, files);
        return ResponseEntity.ok(ApiResponse.success(null, "게시글이 수정되었습니다."));
    }

    @Operation(summary = "게시글 삭제", description = "게시글을 삭제합니다.")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePost(@AuthenticationPrincipal CustomUserDetails userDetails, @PathVariable Long id) {
        postService.deletePost(userDetails.getMemberId(), id);
        return ResponseEntity.ok(ApiResponse.success(null, "게시글이 삭제되었습니다."));
    }
}

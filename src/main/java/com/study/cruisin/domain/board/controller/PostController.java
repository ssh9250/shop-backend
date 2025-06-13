package com.study.cruisin.domain.board.controller;

import com.study.cruisin.domain.board.dto.CreatePostRequestDto;
import com.study.cruisin.domain.board.dto.PostResponseDto;
import com.study.cruisin.domain.board.dto.UpdatePostRequestDto;
import com.study.cruisin.domain.board.service.PostService;
import com.study.cruisin.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class PostController {
    private final PostService postService;

    @PostMapping
    public ResponseEntity<ApiResponse<Long>> createPost(@RequestBody @Valid CreatePostRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.success(postService.createPost(requestDto)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<PostResponseDto>>> getAllPosts() {
        return ResponseEntity.ok(ApiResponse.success(postService.getAllPosts()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PostResponseDto>> getPost(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(postService.getPostById(id)));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> updatePost(@PathVariable Long id, @RequestBody @Valid UpdatePostRequestDto requestDto) {
        postService.updatePost(id, requestDto);
        return ResponseEntity.ok(ApiResponse.success(null, "게시글이 수정되었습니다."));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deletePost(@PathVariable Long id) {
        postService.deletePost(id);
        return ResponseEntity.ok(ApiResponse.success(null, "게시글이 삭제되었습니다."));
    }
}

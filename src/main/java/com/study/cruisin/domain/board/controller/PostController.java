package com.study.cruisin.domain.board.controller;

import com.study.cruisin.domain.board.dto.CreatePostRequestDto;
import com.study.cruisin.domain.board.dto.PostResponseDto;
import com.study.cruisin.domain.board.service.PostService;
import com.study.cruisin.global.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/post")
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
}

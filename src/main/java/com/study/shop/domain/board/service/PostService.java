package com.study.shop.domain.board.service;

import com.study.shop.domain.board.dto.CreatePostRequestDto;
import com.study.shop.domain.board.dto.PostResponseDto;
import com.study.shop.domain.board.dto.UpdatePostRequestDto;
import com.study.shop.domain.board.entity.Post;
import com.study.shop.domain.board.exception.PostNotFoundException;
import com.study.shop.domain.board.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;

    public Long createPost(CreatePostRequestDto request) {
        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .writer(request.getWriter())
                .build();
        return postRepository.save(post).getId();
    }

    @Transactional(readOnly = true)
    public List<PostResponseDto> getAllPosts() {
        return postRepository.findAll()
                .stream()
                .map(PostResponseDto::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public PostResponseDto getPostById(Long id) {
        return postRepository.findById(id)
                .map(PostResponseDto::from)
                .orElseThrow(() -> new PostNotFoundException(id));
    }

    public void updatePost(Long id, UpdatePostRequestDto requestDto) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(id));
        post.update(requestDto.getTitle(), requestDto.getContent());
    }

    public void deletePost(Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(id));
        postRepository.delete(post);
    }
}

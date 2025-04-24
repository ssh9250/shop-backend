package com.study.cruisin.domain.board.service;

import com.study.cruisin.domain.board.dto.CreatePostRequestDto;
import com.study.cruisin.domain.board.dto.PostResponseDto;
import com.study.cruisin.domain.board.entity.Post;
import com.study.cruisin.domain.board.rpository.PostRepository;
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
}

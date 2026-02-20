package com.study.shop.domain.post.service;

import com.study.shop.domain.member.entity.Member;
import com.study.shop.domain.member.exception.MemberNotFoundException;
import com.study.shop.domain.member.repository.MemberRepository;
import com.study.shop.domain.post.dto.CreatePostRequestDto;
import com.study.shop.domain.post.dto.PostResponseDto;
import com.study.shop.domain.post.dto.UpdatePostRequestDto;
import com.study.shop.domain.post.entity.Post;
import com.study.shop.domain.post.exception.PostNotFoundException;
import com.study.shop.domain.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    //  todo: 여기서부터
    public Long createPost(Long memberId, CreatePostRequestDto request) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));

        Post post = Post.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .member(member)
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

    public void updatePost(Long memberId, Long id, UpdatePostRequestDto requestDto) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(id));
        post.update(requestDto.getTitle(), requestDto.getContent());
    }

    public void deletePost(Long memberId, Long id) {
        Post post = postRepository.findById(id)
                .orElseThrow(() -> new PostNotFoundException(id));
        postRepository.delete(post);
    }

    public void validatePostAccess(Long memberId, Post post) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));

        if (!post.getMember().getId().equals(memberId)) {
            throw new AccessDeniedException("해당 작업을 수행할 권한이 없습니다.");
        }
    }
}

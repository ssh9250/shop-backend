package com.study.shop.domain.post.service;

import com.study.shop.domain.member.entity.Member;
import com.study.shop.domain.member.exception.MemberNotFoundException;
import com.study.shop.domain.member.repository.MemberRepository;
import com.study.shop.domain.post.dto.*;
import com.study.shop.domain.post.entity.Post;
import com.study.shop.domain.post.exception.PostNotFoundException;
import com.study.shop.domain.post.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Service
@Transactional
@RequiredArgsConstructor
public class PostService {
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;
    private final FileStorageService fileStorageService;

    public Long createPost(Long memberId, CreatePostRequestDto request, List<MultipartFile> files) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));

        Post post = Post.create(request.getTitle(), request.getContent(), member);

        if (files != null && !files.isEmpty()) {
            files.forEach(file -> {
                try {
                    post.addPostFile(fileStorageService.storeFile(file));
                } catch (IOException e) {
                    throw new RuntimeException("파일 저장 중 오류가 발생했습니다: " + file.getOriginalFilename());
                }
            });
        }
        postRepository.save(post);

        return post.getId();
    }

    @Transactional(readOnly = true)
    public Page<PostListDto> getAllPosts(Pageable pageable) {
        return postRepository.findAllPosts(pageable);
    }

    // todo: filter 적용된 searched post 목록 구현하기 + 단건 조회 첨부파일 구현

    @Transactional(readOnly = true)
    public PostDetailDto getPostById(Long id) {

        return postRepository.findById(id)
                .map(PostDetailDto::from)
                .orElseThrow(() -> new PostNotFoundException(id));
    }

    public void updatePost(Long memberId, Long postId, UpdatePostRequestDto requestDto, List<MultipartFile> files) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));
        // 역시 불필요코드, validate 에서 이미 불러오고, 그 전에 토큰에서 한번 걸러지므로.

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));

        validatePostAccess(memberId, post);

        post.update(requestDto.getTitle(), requestDto.getContent());

        post.getPostFiles().forEach(file -> {
            fileStorageService.deleteFile(file.getStoredFileName());
        });
        post.getPostFiles().clear();

        if (files != null && !files.isEmpty()) {
            files.forEach(file -> {
                try {
                    post.addPostFile(fileStorageService.storeFile(file));
                } catch (IOException e) {
                    throw new RuntimeException("파일 저장 중 오류가 발생했습니다: " + file.getOriginalFilename());
                }
            });
        }
//        postRepository.save(post);
//        필요없음 @Transactional, 즉 트랜잭션이 끝날 때 JPA가 더티체킹으로 자동 업데이트
    }

    public void deletePost(Long memberId, Long postId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException(postId));

        validatePostAccess(memberId, post);

        post.getPostFiles().forEach(file -> {
            fileStorageService.deleteFile(file.getStoredFileName());
        });

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

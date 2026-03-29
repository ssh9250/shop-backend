package com.study.shop.admin.controller;

import com.study.shop.domain.auth.service.AuthService;
import com.study.shop.domain.member.entity.Member;
import com.study.shop.domain.member.repository.MemberRepository;
import com.study.shop.domain.post.dto.CreatePostRequestDto;
import com.study.shop.domain.post.entity.Post;
import com.study.shop.domain.post.repository.PostRepository;
import com.study.shop.domain.post.service.PostService;
import com.study.shop.global.enums.RoleType;
import com.study.shop.security.auth.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Profile("local")
@RestController
@RequiredArgsConstructor
@RequestMapping("/init")
@Tag(name = "Init", description = "테스트 편의를 위한 초기화 API")
public class InitController {
    private final MemberRepository memberRepository;
    private final PostService postService;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/admin")
    @Operation(summary = "관리자 생성")
    public ResponseEntity<String> createAdmin() {
        String s = "string";
        if (memberRepository.existsByEmail(s)) {
            return ResponseEntity.ok("이미 존재합니다.");
        }
        Member admin = Member.builder()
                .email(s)
                .password(passwordEncoder.encode(s))
                .nickname("admin")
                .address("here")
                .role(RoleType.ADMIN)
                .build();
        memberRepository.save(admin);
        return ResponseEntity.ok("admin created");
    }

    @PostMapping("/notice")
    @Operation(summary = "공지 작성")
    public ResponseEntity<String> createNotice(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Post notice = Post.builder()
                .title("공지사항")
                .content("공지사항입니다.")
                .build();

        CreatePostRequestDto dto = CreatePostRequestDto
                .builder()
                .title("공지사항")
                .content("공지사항입니다.")
                .build();

        postService.createPost(userDetails.getMemberId(), dto, null);
        return ResponseEntity.ok("notice created");
    }
}

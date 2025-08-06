package com.study.shop.domain.member.controller;

import com.study.shop.domain.board.service.PostService;
import com.study.shop.domain.comment.service.CommentService;
import com.study.shop.domain.member.dto.ChangePasswordRequestDto;
import com.study.shop.domain.member.dto.MemberResponseDto;
import com.study.shop.domain.member.dto.UpdateProfileRequestDto;
import com.study.shop.domain.member.service.MemberService;
import com.study.shop.global.response.ApiResponse;
import com.study.shop.global.security.auth.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberController {
    private final MemberService memberService;
    private final PostService postService;
    private final CommentService commentService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MemberResponseDto>> getMember(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long id = userDetails.getMember().getId();
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(memberService.getMemberById(id)));
    }

    @PatchMapping("/profile")
    public ResponseEntity<ApiResponse<Void>> updateProfile(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody UpdateProfileRequestDto requestDto) {
        Long id = userDetails.getMember().getId();
        memberService.updateProfile(id, requestDto);
        return ResponseEntity.ok(ApiResponse.success(null, "회원정보가 수정되었습니다."));
    }

    @PatchMapping("/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody ChangePasswordRequestDto requestDto) {
        Long id = userDetails.getMember().getId();
        memberService.updatePassword(id, requestDto);
        return ResponseEntity.ok(ApiResponse.success(null, "비밀번호가 수정되었습니다."));
    }

    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteMember(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long id = userDetails.getMember().getId();
        memberService.deleteMember(id);
        return ResponseEntity.ok(ApiResponse.success(null, "회원을 탈퇴하였습니다."));
    }

}

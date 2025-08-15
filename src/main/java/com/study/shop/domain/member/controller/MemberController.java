package com.study.shop.domain.member.controller;

import com.study.shop.domain.post.service.PostService;
import com.study.shop.domain.comment.service.CommentService;
import com.study.shop.domain.member.dto.ChangePasswordRequestDto;
import com.study.shop.domain.member.dto.MemberResponseDto;
import com.study.shop.domain.member.dto.UpdateProfileRequestDto;
import com.study.shop.domain.member.service.MemberService;
import com.study.shop.global.response.ApiResponse;
import com.study.shop.global.security.auth.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
@Tag(name = "Member", description = "회원 관련 API")
public class MemberController {
    private final MemberService memberService;
    private final PostService postService;
    private final CommentService commentService;

    @Operation(summary = "사용자 LONG ID 조회", description = "세션 필요<br>현재 로그인된 사용자의 ID 정보를 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MemberResponseDto>> getMember(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long id = userDetails.getMember().getId();
        return ResponseEntity.ok(ApiResponse.success(memberService.getMemberById(id)));
    }

    // 회원 개인정보 조회

    @Operation(summary = "개인정보 수정", description = "회원정보를 수정합니다.")
    @PatchMapping("/profile")
    public ResponseEntity<ApiResponse<Void>> updateProfile(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody UpdateProfileRequestDto requestDto) {
        Long id = userDetails.getMember().getId();
        memberService.updateProfile(id, requestDto);
        return ResponseEntity.ok(ApiResponse.success(null, "회원정보가 수정되었습니다."));
    }

    @Operation(summary = "비밀번호 변경", description = "비밀번호를 변경합니다.")
    @PatchMapping("/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody ChangePasswordRequestDto requestDto) {
        Long id = userDetails.getMember().getId();
        memberService.updatePassword(id, requestDto);
        return ResponseEntity.ok(ApiResponse.success(null, "비밀번호가 수정되었습니다."));
    }

    @Operation(summary = "회원 탈퇴", description = "사용자 정보를 삭제합니다.")
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteMember(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long id = userDetails.getMember().getId();
        memberService.deleteMember(id);
        return ResponseEntity.ok(ApiResponse.success(null, "회원을 탈퇴하였습니다."));
    }

}

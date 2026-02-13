package com.study.shop.domain.member.controller;

import com.study.shop.domain.member.dto.ChangePasswordRequestDto;
import com.study.shop.domain.member.dto.MemberResponseDto;
import com.study.shop.domain.member.dto.UpdateProfileRequestDto;
import com.study.shop.domain.member.service.MemberService;
import com.study.shop.global.response.ApiResponse;
import com.study.shop.security.auth.CustomUserDetails;
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

    @Operation(summary = "내 정보 조회", description = "사용자의 정보를 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MemberResponseDto>> getMember(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long id = userDetails.getMemberId();
        return ResponseEntity.ok(ApiResponse.success(memberService.getMemberById(id)));
    }

    @Operation(summary = "개인정보 수정", description = "회원 정보를 수정합니다.")
    @PatchMapping("/profile")
    public ResponseEntity<ApiResponse<Void>> updateProfile(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody UpdateProfileRequestDto requestDto) {
        Long id = userDetails.getMemberId();
        memberService.updateProfile(id, requestDto);
        return ResponseEntity.ok(ApiResponse.success(null, "회원정보 수정 완료"));
    }

    @Operation(summary = "비밀번호 변경", description = "비밀번호를 변경합니다.")
    @PatchMapping("/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@AuthenticationPrincipal CustomUserDetails userDetails, @RequestBody ChangePasswordRequestDto requestDto) {
        Long id = userDetails.getMemberId();
        memberService.updatePassword(id, requestDto);
        return ResponseEntity.ok(ApiResponse.success(null, "비밀번호 수정 완료"));
    }

    @Operation(summary = "회원 탈퇴", description = "회원 정보를 삭제합니다. 연관관계를 맺은 모든 데이터들은 영속성 전이를 통해 모두 삭제됩니다.")
    @DeleteMapping
    public ResponseEntity<ApiResponse<Void>> deleteMember(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long id = userDetails.getMemberId();
        memberService.deleteMember(id);
        return ResponseEntity.ok(ApiResponse.success(null, "회원 탈퇴 완료"));
    }

}

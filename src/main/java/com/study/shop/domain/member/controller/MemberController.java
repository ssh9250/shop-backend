package com.study.shop.domain.member.controller;

import com.study.shop.domain.board.service.PostService;
import com.study.shop.domain.comment.service.CommentService;
import com.study.shop.domain.member.dto.ChangePasswordRequestDto;
import com.study.shop.domain.member.dto.CreateMemberRequestDto;
import com.study.shop.domain.member.dto.MemberResponseDto;
import com.study.shop.domain.member.dto.UpdateProfileRequestDto;
import com.study.shop.domain.member.service.MemberService;
import com.study.shop.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class MemberController {
    private final MemberService memberService;
    private final PostService postService;
    private final CommentService commentService;

    @PostMapping
    public ResponseEntity<ApiResponse<Long>> createMember(@RequestBody CreateMemberRequestDto requestDto) {
        return ResponseEntity.ok(ApiResponse.success(memberService.createMember(requestDto)));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<MemberResponseDto>> getMember(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(memberService.getMemberById(id)));
    }

    @PatchMapping("/{id}/profile")
    public ResponseEntity<ApiResponse<Void>> updateProfile(@PathVariable Long id, @RequestBody UpdateProfileRequestDto requestDto) {
        memberService.updateProfile(id, requestDto);
        return ResponseEntity.ok(ApiResponse.success(null, "회원정보가 수정되었습니다."));
    }

    @PatchMapping("/{id}/password")
    public ResponseEntity<ApiResponse<Void>> changePassword(@PathVariable Long id, @RequestBody ChangePasswordRequestDto requestDto) {
        memberService.updatePassword(id, requestDto);
        return ResponseEntity.ok(ApiResponse.success(null, "비밀번호가 수정되었습니다."));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteMember(@PathVariable Long id) {
        memberService.deleteMember(id);
        return ResponseEntity.ok(ApiResponse.success(null, "회원을 탈퇴하였습니다."));
    }

}

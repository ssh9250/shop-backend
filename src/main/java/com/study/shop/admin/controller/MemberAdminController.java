package com.study.shop.admin.controller;

import com.study.shop.admin.service.MemberAdminService;
import com.study.shop.domain.member.dto.MemberListResponseDto;
import com.study.shop.domain.member.dto.MemberSearchConditionDto;
import com.study.shop.global.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/member")
@PreAuthorize("hasRole('ADMIN')")
@Tag(name = "Member Admin", description = "관리자 회원 관리 API")
public class MemberAdminController {
    private final MemberAdminService memberAdminService;

    @Operation(summary = "전체 회원정보 조회", description = "조건에 맞는 회원 리스트를 조회합니다. 조건이 없으면 탈퇴한 회원을 포함한 전체 회원을 조회합니다.")
    @GetMapping()
    public ResponseEntity<ApiResponse<List<MemberListResponseDto>>> getMemberList(@ModelAttribute MemberSearchConditionDto cond) {
        return ResponseEntity.ok(ApiResponse.success(memberAdminService.memberList(cond)));
    }

    @PostMapping("/kick/{id}")
    public ResponseEntity<ApiResponse<Long>> kickoutMember(@PathVariable Long id) {
        memberAdminService.kickMember(id);
        return ResponseEntity.ok(ApiResponse.success(null, "회원 추방 완료"));
    }
}

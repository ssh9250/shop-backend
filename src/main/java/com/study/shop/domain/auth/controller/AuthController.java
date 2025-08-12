package com.study.shop.domain.auth.controller;

import com.study.shop.domain.auth.dto.LoginRequestDto;
import com.study.shop.domain.auth.dto.LoginResponseDto;
import com.study.shop.domain.auth.dto.SignupRequestDto;
import com.study.shop.domain.auth.service.AuthService;
import com.study.shop.domain.member.dto.MemberResponseDto;
import com.study.shop.domain.member.service.MemberService;
import com.study.shop.global.response.ApiResponse;
import com.study.shop.global.security.auth.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "로그인/세션 관련 API")
public class AuthController {
    private final AuthService authService;
    private final MemberService memberService;

    @Operation(summary = "로그인 요청", description = "ID, PW로 로그인을 시도합니다.")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDto>> login(@RequestBody LoginRequestDto requestDto) throws Exception {
        LoginResponseDto responseDto = authService.login(requestDto);
        return ResponseEntity.ok(ApiResponse.success(responseDto, "로그인 성공"));
    }

    @Operation(summary = "회원가입 요청", description = "회원가입을 요청합니다.")
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignupRequestDto>> signup(@RequestBody SignupRequestDto requestDto) throws Exception {
        authService.signup(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}

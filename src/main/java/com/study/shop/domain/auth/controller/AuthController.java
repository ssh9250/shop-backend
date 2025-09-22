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

    @Operation(summary = "회원가입", description = "신규 회원을 등록합니다.")
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<SignupRequestDto>> signup(@RequestBody SignupRequestDto requestDto) throws Exception {
        authService.signup(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(null, "회원가입에 성공하였습니다."));
    }

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인합니다.")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDto>> login(@RequestBody LoginRequestDto requestDto) throws Exception {
        LoginResponseDto responseDto = authService.login(requestDto);
        return ResponseEntity.ok(ApiResponse.success(responseDto, "로그인 성공"));
    }
}

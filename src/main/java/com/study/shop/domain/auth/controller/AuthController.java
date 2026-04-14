package com.study.shop.domain.auth.controller;

import com.study.shop.domain.auth.dto.LoginRequestDto;
import com.study.shop.domain.auth.dto.LoginResponseDto;
import com.study.shop.domain.auth.dto.SignupRequestDto;
import com.study.shop.domain.auth.service.AuthService;
import com.study.shop.domain.member.service.MemberService;
import com.study.shop.global.response.ApiResponse;
import com.study.shop.security.auth.CustomUserDetails;
import com.study.shop.security.dto.RefreshRequestDto;
import com.study.shop.security.dto.RefreshResponseDto;
import com.study.shop.security.jwt.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "로그인/세션 관련 API")
@Slf4j
public class AuthController {
    private final AuthService authService;
    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;

    @Operation(summary = "회원가입", description = "신규 회원을 등록합니다. 이메일·닉네임 중복 시 409 반환.", security = {})
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signup(@Valid @RequestBody SignupRequestDto requestDto) throws Exception {
        log.info("signup attempt for email: {}", requestDto.getEmail());
        authService.signup(requestDto);
        log.info("signup success for email: {}", requestDto.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(null, "회원가입 성공"));
    }

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인합니다. 성공 시 Access Token(30분)과 Refresh Token(14일)을 반환합니다.", security = {})
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDto>> login(@Valid @RequestBody LoginRequestDto requestDto) {
        log.info("login attempt for email: {}", requestDto.getEmail());
        LoginResponseDto responseDto = authService.login(requestDto);
        log.info("login success for email: {}", requestDto.getEmail());
        return ResponseEntity.ok(ApiResponse.success(responseDto, "로그인 성공"));
    }

    @Operation(summary = "토큰 갱신", description = "Refresh Token으로 Access/Refresh Token을 재발급합니다. (Rotation 방식 — 기존 Refresh Token은 만료됩니다.)", security = {})
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<RefreshResponseDto>> refresh(@RequestBody RefreshRequestDto requestDto) {
        log.info("refresh attempt");
        RefreshResponseDto responseDto = authService.refresh(requestDto.getRefreshToken());
        log.info("refresh success");
        return new ResponseEntity<>(ApiResponse.success(responseDto), HttpStatus.OK);
    }

    @Operation(summary = "로그아웃", description = "현재 Access Token을 블랙리스트에 등록하여 즉시 무효화합니다.", security = @SecurityRequirement(name = "bearerAuth"))
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal CustomUserDetails customUserDetails, HttpServletRequest request) {
        String email = customUserDetails.getUsername();
        String accessToken = jwtTokenProvider.resolveToken(request);

        log.info("logout attempt for email: {}", email);
        authService.logout(accessToken, email);
        log.info("logout success for email: {}", email);

        return ResponseEntity.ok(ApiResponse.success(null, "로그아웃 성공"));
    }
}

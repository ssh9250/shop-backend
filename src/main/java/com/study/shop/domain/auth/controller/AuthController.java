package com.study.shop.domain.auth.controller;

import com.study.shop.domain.auth.dto.LoginRequestDto;
import com.study.shop.domain.auth.dto.LoginResponseDto;
import com.study.shop.domain.auth.dto.SignupRequestDto;
import com.study.shop.domain.auth.service.AuthService;
import com.study.shop.domain.member.dto.MemberResponseDto;
import com.study.shop.domain.member.service.MemberService;
import com.study.shop.global.response.ApiResponse;
import com.study.shop.global.security.auth.CustomUserDetails;
import com.study.shop.global.security.dto.RefreshRequestDto;
import com.study.shop.global.security.dto.RefreshResponseDto;
import com.study.shop.global.security.jwt.JwtTokenProvider;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
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

    @Operation(summary = "회원가입", description = "신규 회원을 등록합니다.")
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signup(@RequestBody SignupRequestDto requestDto) throws Exception {
        log.info("signup attempt for email: {}", requestDto.getEmail());
        authService.signup(requestDto);
        log.info("signup success for email: {}", requestDto.getEmail());
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(null, "회원가입 성공"));
    }

    @Operation(summary = "로그인", description = "이메일과 비밀번호로 로그인합니다.")
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponseDto>> login(@RequestBody LoginRequestDto requestDto) {
        log.info("login attempt for email: {}", requestDto.getEmail());
        LoginResponseDto responseDto = authService.login(requestDto);
        log.info("login success for email: {}", requestDto.getEmail());
        return ResponseEntity.ok(ApiResponse.success(responseDto, "로그인 성공"));
    }

    @Operation(summary = "토큰 갱신", description = "Refresh Token으로 Access Token을 갱신합니다.")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<RefreshResponseDto>> refresh(@RequestBody RefreshRequestDto requestDto) {
        log.info("refresh attempt");
        RefreshResponseDto responseDto = authService.refresh(requestDto.getRefreshToken());
        log.info("refresh success");
        return new ResponseEntity<>(ApiResponse.success(responseDto), HttpStatus.OK);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@AuthenticationPrincipal CustomUserDetails customUserDetails, HttpServletRequest request) {
        String email = customUserDetails.getUsername();
        String accessToken = jwtTokenProvider.resolveToken(request);

        if (accessToken == null) {
            log.error("token is empty for email: {}", email);
            return ResponseEntity.badRequest().body(ApiResponse.fail("토큰이 없습니다."));
        }

        log.info("logout attempt for email: {}", email);
        authService.logout(accessToken, email);
        log.info("logout success for email: {}", email);

        return ResponseEntity.ok(ApiResponse.success(null, "로그아웃 성공"));
    }
}

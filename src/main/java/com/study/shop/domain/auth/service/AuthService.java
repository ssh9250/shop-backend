package com.study.shop.domain.auth.service;

import com.study.shop.domain.auth.dto.LoginRequestDto;
import com.study.shop.domain.auth.dto.LoginResponseDto;
import com.study.shop.domain.auth.dto.SignupRequestDto;
import com.study.shop.domain.member.service.MemberService;
import com.study.shop.global.security.auth.CustomUserDetails;
import com.study.shop.global.security.dto.RefreshResponseDto;
import com.study.shop.global.security.exception.InvalidTokenException;
import com.study.shop.global.security.exception.RefreshTokenMismatchException;
import com.study.shop.global.security.jwt.JwtTokenProvider;
import com.study.shop.global.security.refresh.RefreshTokenService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    private final AuthenticationManager authenticationManager;
    private final MemberService memberService;
    private final JwtTokenProvider jwtTokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final StringRedisTemplate stringRedisTemplate;

    public void signup(SignupRequestDto requestDto) throws Exception {
        memberService.signup(requestDto);
    }

    public LoginResponseDto login(LoginRequestDto loginRequestDto) throws RuntimeException {
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(loginRequestDto.getEmail(), loginRequestDto.getPassword());

        Authentication authentication = authenticationManager.authenticate(authToken);

        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();

        String accessToken = jwtTokenProvider.createAccessToken(customUserDetails.getEmail());
        String refreshToken = refreshTokenService.createAndStoreRefreshToken(jwtTokenProvider.createRefreshToken(customUserDetails.getEmail()));

        return LoginResponseDto.builder()
                .memberId(customUserDetails.getMemberId())
                .email(customUserDetails.getUsername())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    // Refresh Token으로 Access Token 갱신
    public RefreshResponseDto refresh(String refreshToken) {
        if (!jwtTokenProvider.validateToken(refreshToken)) {
            log.error("Invalid refresh token");
            throw new InvalidTokenException();
        }

        String email = jwtTokenProvider.getEmail(refreshToken);

        // Redis 토큰과 비교
        if (!refreshTokenService.validateRefreshToken(email, refreshToken)) {
            log.error("Refresh token mismatch");
            throw new RefreshTokenMismatchException();
        }

        String newAccessToken = jwtTokenProvider.createAccessToken(email);
        String newRefreshToken = refreshTokenService.rotateRefreshToken(email, refreshToken);

        return RefreshResponseDto.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    public void logout(String accessToken, String email) {
        refreshTokenService.removeRefreshToken(email);
        log.info("refresh token removed from email: {}", email);

        stringRedisTemplate.setb
    }
}

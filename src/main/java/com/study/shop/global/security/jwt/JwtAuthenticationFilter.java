package com.study.shop.global.security.jwt;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import lombok.RequiredArgsConstructor;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtTokenProvider jwtTokenProvider;
    private final StringRedisTemplate stringRedisTemplate;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        // 1. authorization 헤더에서 토큰 추출
        String token = jwtTokenProvider.resolveToken(request);

        // 토큰 없으면 -> 다음 필터로 넘기기
        if (!StringUtils.isEmpty(token)) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. redis 블랙리스트 검사 (reids에 키 존재하면 로그아웃된 토큰임)
        String blacklistKey = "blacklist" + token;
        Boolean isBlacklist = stringRedisTemplate.hasKey(blacklistKey);

        if (isBlacklist) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. access token 유효성 검증
        if (jwtTokenProvider.validateToken(token)) {
            // 3-1. 정상 토큰 -> 인증 생성
            Authentication authentication = jwtTokenProvider.getAuthentication(token);

            // 3-2. security context에 설정
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } else {
            // 4. 만료된 토큰 -> refresh token 검사
//            SecurityContextHolder.clearContext();

        }
        // 5. 다음 필터 실행
        filterChain.doFilter(request, response);
    }
}

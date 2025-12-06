package com.study.shop.global.security.jwt;

import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * JWT 예외를 처리하는 필터
 * JwtAuthenticationFilter에서 발생하는 예외를 잡아 적절한 응답 반환
 */
public class JwtExceptionFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        try {
            filterChain.doFilter(request, response);
        } catch (JwtException e) {
            // JWT 관련 예외 발생 시 401 Unauthorized 응답
            setErrorResponse(response, e.getMessage());
        }
    }

    private void setErrorResponse(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(
                String.format("{\"error\": \"인증 실패\", \"message\": \"%s\"}", message)
//                기타 front에서 요구하는 응답 포멧 작성 예정
        );
    }
}

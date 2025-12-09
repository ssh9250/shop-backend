package com.study.shop.global.security.handler;

import com.study.shop.global.exception.ErrorCode;
import com.study.shop.global.security.util.SecurityResponseUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 인증되지 않은 사용자가 보호된 리소스에 접근할 때 호출 (401)
 */
@Slf4j
@Component
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException {

        log.warn("Unauthorized access attempt: {} - {}",
                request.getRequestURI(),
                authException.getMessage());

        // 요청에서 에러 정보 확인 (JwtExceptionFilter에서 설정한 경우)
        ErrorCode errorCode = (ErrorCode) request.getAttribute("errorCode");

        if (errorCode != null) {
            // JwtExceptionFilter에서 설정한 구체적인 에러 코드 사용
            SecurityResponseUtil.sendErrorResponse(response, errorCode);
        } else {
            // 기본 인증 실패 에러
            SecurityResponseUtil.sendErrorResponse(response, ErrorCode.UNAUTHORIZED);
        }
    }
}
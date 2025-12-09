package com.study.shop.global.security.handler;

import com.study.shop.global.exception.ErrorCode;
import com.study.shop.global.security.util.SecurityResponseUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * 인증은 되었지만 권한이 없는 사용자가 접근할 때 호출 (403)
 */
@Slf4j
@Component
public class JwtAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException accessDeniedException
    ) throws IOException {

        log.warn("Access denied: {} - {} - User: {}",
                request.getRequestURI(),
                accessDeniedException.getMessage(),
                request.getUserPrincipal() != null
                        ? request.getUserPrincipal().getName()
                        : "anonymous");

        SecurityResponseUtil.sendErrorResponse(response, ErrorCode.ACCESS_DENIED);
    }
}
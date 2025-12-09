package com.study.shop.global.security.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.shop.global.exception.ErrorCode;
import com.study.shop.global.response.ApiResponse;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;

@Slf4j
public class SecurityResponseUtil {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public static void sendErrorResponse(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        log.error("Security Error: {} - {}", errorCode.name(), errorCode.getMessage());

        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(errorCode.getStatus().value());

        ApiResponse<Void> apiResponse = ApiResponse.fail(errorCode.getMessage());
        String jsonResponse = OBJECT_MAPPER.writeValueAsString(apiResponse);

        response.getWriter().write(jsonResponse);
    }

    /**
     * 추가 정보가 필요한 경우
     */
    public static void sendErrorResponse(
            HttpServletResponse response,
            ErrorCode errorCode,
            String additionalInfo
    ) throws IOException {

        log.error("Security Error: {} - {} ({})",
                errorCode.name(), errorCode.getMessage(), additionalInfo);

        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(errorCode.getStatus().value());

        ApiResponse<Void> apiResponse = ApiResponse.fail(
                errorCode.getMessage() + " (" + additionalInfo + ")"
        );
        String jsonResponse = OBJECT_MAPPER.writeValueAsString(apiResponse);

        response.getWriter().write(jsonResponse);
    }
}

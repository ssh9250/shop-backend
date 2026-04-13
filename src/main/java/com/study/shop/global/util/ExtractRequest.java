package com.study.shop.global.util;

import jakarta.servlet.http.HttpServletRequest;

public class ExtractRequest {
    private ExtractRequest() {
    }

    public static String extractIp(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip != null && !ip.isEmpty()) {
            return ip.split(",")[0].trim(); // 프록시 ip 걸러내기용
        }
        return request.getRemoteAddr();
    }
}

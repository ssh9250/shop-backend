package com.study.shop.global.security.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RefreshResponseDto {
    private String accessToken;
    private String refreshToken;
}

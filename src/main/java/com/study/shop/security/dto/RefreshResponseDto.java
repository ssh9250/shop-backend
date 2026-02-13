package com.study.shop.security.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RefreshResponseDto {
    private String accessToken;
    private String refreshToken;
}

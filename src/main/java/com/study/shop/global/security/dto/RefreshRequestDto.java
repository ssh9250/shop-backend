package com.study.shop.global.security.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class RefreshRequestDto {
    private String refreshToken;
}

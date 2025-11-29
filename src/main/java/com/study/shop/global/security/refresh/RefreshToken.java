package com.study.shop.global.security.refresh;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Schema
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {
    private String email;
    private String token;
    private long expiresAt;

}

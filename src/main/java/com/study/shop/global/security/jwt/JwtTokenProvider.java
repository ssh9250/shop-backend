package com.study.shop.global.security.jwt;

import com.study.shop.global.security.auth.CustomUserDetailsService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;

@Component
@RequiredArgsConstructor
public class JwtTokenProvider {
    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration}")
    private Long validityInMs;

    private final CustomUserDetailsService customUserDetailsService;

    private Key getSigningKey(){
        return Keys.hmac
    }
}

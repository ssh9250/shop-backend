package com.study.shop.domain.auth.service;

import com.study.shop.domain.auth.dto.LoginRequestDto;
import com.study.shop.domain.auth.dto.LoginResponseDto;
import com.study.shop.global.security.auth.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class AuthService {
    private final AuthenticationManager authenticationManager;

    public LoginResponseDto login(LoginRequestDto loginRequestDto) throws RuntimeException {
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(loginRequestDto.getEmail(), loginRequestDto.getPassword());

        Authentication authentication = authenticationManager.authenticate(authToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);


//        CustomUserDetails customUserDetails = (CustomUserDetails) authentication.getPrincipal();

        String token = "jwt-token";

        // 좀 더 단순화 (최소한의 기능만) + oauth 등 지원

        return LoginResponseDto.builder()
                .token(token)
                .build();
    }
}

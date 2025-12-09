package com.study.shop.global.security.refresh;

import com.study.shop.global.security.exception.RefreshTokenMismatchException;
import com.study.shop.global.security.exception.RefreshTokenNotFoundException;
import com.study.shop.global.security.jwt.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class RefreshTokenService {
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;

    @Value("${jwt.refresh-expiration-ms}")
    private long refreshTokenValidityMs;

    //
    public String createAndStoreRefreshToken(String email) {
        String refreshToken = jwtTokenProvider.createRefreshToken(email);
        refreshTokenRepository.store(email, refreshToken,  refreshTokenValidityMs);
        return refreshToken;
    }

    public boolean validateRefreshToken(String email, String refreshToken) {
        if (email == null || refreshToken == null)
            return false;

        String saved = refreshTokenRepository.findByEmail(email);
        if (saved == null)
            return false;
        if (!saved.equals(refreshToken))
            return false;

        return jwtTokenProvider.validateToken(refreshToken);
    }

    public String rotateRefreshToken(String email, String oldRefreshToken) {
        String saved = refreshTokenRepository.findByEmail(email);

        if (saved == null) {
            log.error("Refresh token not found");
            throw new RefreshTokenNotFoundException();
        }

        if (!saved.equals(oldRefreshToken)) {
            log.error("Refresh token does not match");
            refreshTokenRepository.delete(email);
            throw new RefreshTokenMismatchException();
        }

        String newRefreshToken = jwtTokenProvider.createRefreshToken(email);
        refreshTokenRepository.store(email, newRefreshToken,  refreshTokenValidityMs);
        return newRefreshToken;
    }

    public void removeRefreshToken(String email) {
        refreshTokenRepository.delete(email);
    }

    public boolean hasRefreshToken(String email) {
        return refreshTokenRepository.findByEmail(email) != null;
    }
}

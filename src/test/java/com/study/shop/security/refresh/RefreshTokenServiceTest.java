package com.study.shop.security.refresh;

import com.study.shop.security.exception.RefreshTokenMismatchException;
import com.study.shop.security.exception.RefreshTokenNotFoundException;
import com.study.shop.security.jwt.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

// todo: 이거 보기
@ExtendWith(MockitoExtension.class)
class RefreshTokenServiceTest {

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @InjectMocks
    private RefreshTokenService refreshTokenService;

    private static final String EMAIL = "test@example.com";
    private static final String TOKEN = "valid.refresh.token";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(refreshTokenService, "refreshTokenValidityMs", 604800000L);
    }

    // ── validateRefreshToken ────────────────────────────────────────────────

    @Test
    @DisplayName("validateRefreshToken: email이 null이면 false 반환")
    void validateRefreshToken_emailNull_returnsFalse() {
        assertThat(refreshTokenService.validateRefreshToken(null, TOKEN)).isFalse();
        verifyNoInteractions(refreshTokenRepository);
    }

    @Test
    @DisplayName("validateRefreshToken: refreshToken이 null이면 false 반환")
    void validateRefreshToken_tokenNull_returnsFalse() {
        assertThat(refreshTokenService.validateRefreshToken(EMAIL, null)).isFalse();
        verifyNoInteractions(refreshTokenRepository);
    }

    @Test
    @DisplayName("validateRefreshToken: 저장된 토큰과 불일치하면 false 반환")
    void validateRefreshToken_tokenMismatch_returnsFalse() {
        when(refreshTokenRepository.findByEmail(EMAIL)).thenReturn("other.token");

        assertThat(refreshTokenService.validateRefreshToken(EMAIL, TOKEN)).isFalse();
        verifyNoInteractions(jwtTokenProvider);
    }

    // ── rotateRefreshToken ──────────────────────────────────────────────────

    @Test
    @DisplayName("rotateRefreshToken: 저장된 토큰이 없으면 RefreshTokenNotFoundException 발생")
    void rotateRefreshToken_notFound_throwsException() {
        when(refreshTokenRepository.findByEmail(EMAIL)).thenReturn(null);

        assertThatThrownBy(() -> refreshTokenService.rotateRefreshToken(EMAIL, TOKEN))
                .isInstanceOf(RefreshTokenNotFoundException.class);
        verify(refreshTokenRepository, never()).delete(any());
    }

    @Test
    @DisplayName("rotateRefreshToken: 토큰 불일치 시 저장된 토큰 삭제 후 RefreshTokenMismatchException 발생")
    void rotateRefreshToken_mismatch_deletesAndThrowsException() {
        when(refreshTokenRepository.findByEmail(EMAIL)).thenReturn("other.token");

        assertThatThrownBy(() -> refreshTokenService.rotateRefreshToken(EMAIL, TOKEN))
                .isInstanceOf(RefreshTokenMismatchException.class);
        verify(refreshTokenRepository).delete(EMAIL);
    }

    // ── hasRefreshToken ─────────────────────────────────────────────────────

    @Test
    @DisplayName("hasRefreshToken: 토큰이 존재하면 true 반환")
    void hasRefreshToken_exists_returnsTrue() {
        when(refreshTokenRepository.exists(EMAIL)).thenReturn(true);

        assertThat(refreshTokenService.hasRefreshToken(EMAIL)).isTrue();
    }

    @Test
    @DisplayName("hasRefreshToken: 토큰이 없으면 false 반환")
    void hasRefreshToken_notExists_returnsFalse() {
        when(refreshTokenRepository.exists(EMAIL)).thenReturn(false);

        assertThat(refreshTokenService.hasRefreshToken(EMAIL)).isFalse();
    }
}
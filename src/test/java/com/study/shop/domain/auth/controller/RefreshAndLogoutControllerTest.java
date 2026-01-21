package com.study.shop.domain.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.study.shop.common.IntegrationTestBase;
import com.study.shop.domain.auth.dto.LoginRequestDto;
import com.study.shop.domain.member.entity.Member;
import com.study.shop.domain.member.repository.MemberRepository;
import com.study.shop.global.enums.RoleType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class RefreshAndLogoutControllerTest extends IntegrationTestBase {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    private final String TEST_EMAIL = "test@example.com";
    private final String TEST_PASSWORD = "password123";
    private final String TEST_NICKNAME = "testUser";
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @BeforeEach
    void setUp() {
        // 테스트용 회원 등록
        Member testMember = Member.builder()
                .email(TEST_EMAIL)
                .password(passwordEncoder.encode(TEST_PASSWORD))
                .nickname(TEST_NICKNAME)
                .role(RoleType.USER)
                .build();
        memberRepository.save(testMember);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();

        stringRedisTemplate.getConnectionFactory().getConnection().flushDb();
    }

    private TokenInfo loginAndGetTokens() throws Exception {
        LoginRequestDto loginRequest = new LoginRequestDto(TEST_EMAIL, TEST_PASSWORD);

        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        String accessToken = JsonPath.read(responseBody, "$.data.accessToken");
        String refreshToken = JsonPath.read(responseBody, "$.data.refreshToken");

        return new TokenInfo(accessToken, refreshToken);
    }

    private record TokenInfo(String accessToken, String refreshToken) {}

    @Nested
    @DisplayName("/api/auth/refresh - 토큰 재발급")
    class Refresh {
        @Test
        @DisplayName("정상 토큰 재발급")
        void refreshSuccess() throws Exception {
            // given - 로그인하여 토큰 획득
            TokenInfo tokenInfo = loginAndGetTokens();

            String requestBody = String.format("{\"refreshToken\": \"%s\"}", tokenInfo.refreshToken());

            // when & then
            mockMvc.perform(post("/api/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.accessToken").exists())
                    .andExpect(jsonPath("$.data.refreshToken").exists());
        }

        @Test
        @DisplayName("유효하지 않은 refreshToken으로 재발급 실패")
        void refreshFail_InvalidToken() throws Exception {
            // given
            String invalidRefreshToken = "invalid.refresh.token";
            String requestBody = String.format("{\"refreshToken\": \"%s\"}", invalidRefreshToken);

            // when & then
            mockMvc.perform(post("/api/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andDo(print())
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("refreshToken 누락 시 재발급 실패")
        void refreshFail_MissingToken() throws Exception {
            // given
            String requestBody = "{}";

            // when & then
            mockMvc.perform(post("/api/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("빈 문자열 refreshToken으로 재발급 실패")
        void refreshFail_EmptyToken() throws Exception {
            // given
            String requestBody = "{\"refreshToken\": \"\"}";

            // when & then
            mockMvc.perform(post("/api/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("/api/auth/logout - 로그아웃")
    class Logout {
        @Test
        @DisplayName("정상 로그아웃")
        void logoutSuccess() throws Exception {
            // given - 로그인하여 토큰 획득
            TokenInfo tokenInfo = loginAndGetTokens();

            // when & then
            mockMvc.perform(post("/api/auth/logout")
                            .header("Authorization", "Bearer " + tokenInfo.accessToken()))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("로그아웃 성공"));

            // redis blacklist 검증
            String blacklistKey = "blacklist:" + tokenInfo.accessToken;
            assertThat(stringRedisTemplate.hasKey(blacklistKey)).isTrue();
        }

        @Test
        @DisplayName("인증 없이 로그아웃 시도 시 실패")
        void logoutFail_Unauthorized() throws Exception {
            // when & then
            mockMvc.perform(post("/api/auth/logout"))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("유효하지 않은 토큰으로 로그아웃 시도 시 실패")
        void logoutFail_InvalidToken() throws Exception {
            // given
            TokenInfo tokenInfo = loginAndGetTokens();
            String invalidToken = "invalid.access.token";

            // when & then
            mockMvc.perform(post("/api/auth/logout")
                            .header("Authorization", "Bearer " + invalidToken))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("로그아웃 후 동일 토큰으로 재요청 시 실패")
        void logoutFail_TokenBlacklisted() throws Exception {
            // given - 로그인하여 토큰 획득
            TokenInfo tokenInfo = loginAndGetTokens();

            // 로그아웃 수행
            mockMvc.perform(post("/api/auth/logout")
                            .header("Authorization", "Bearer " + tokenInfo.accessToken()))
                    .andExpect(status().isOk());

            // when & then - 동일 토큰으로 다시 로그아웃 시도 (블랙리스트에 있어야 함)
            mockMvc.perform(post("/api/auth/logout")
                            .header("Authorization", "Bearer " + tokenInfo.accessToken()))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("로그아웃 후 refreshToken으로 토큰 재발급 실패")
        void logoutFail_RefreshAfterLogout() throws Exception {
            // given - 로그인하여 토큰 획득
            TokenInfo tokenInfo = loginAndGetTokens();

            // 로그아웃 수행
            mockMvc.perform(post("/api/auth/logout")
                            .header("Authorization", "Bearer " + tokenInfo.accessToken()))
                    .andExpect(status().isOk());

            // when & then - 로그아웃 후 refreshToken으로 재발급 시도
            String requestBody = String.format("{\"refreshToken\": \"%s\"}", tokenInfo.refreshToken());

            mockMvc.perform(post("/api/auth/refresh")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestBody))
                    .andDo(print())
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }
}
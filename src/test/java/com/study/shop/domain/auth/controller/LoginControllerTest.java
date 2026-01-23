package com.study.shop.domain.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.shop.common.IntegrationTestBase;
import com.study.shop.domain.auth.dto.LoginRequestDto;
import com.study.shop.domain.member.entity.Member;
import com.study.shop.domain.member.repository.MemberRepository;
import com.study.shop.global.enums.RoleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class LoginControllerTest extends IntegrationTestBase {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;


    @Nested
    @DisplayName("/api/auth/login -로그인")
    class Login {
        private final String TEST_EMAIL = "test@example.com";
        private final String TEST_PASSWORD = "password123";

        @BeforeEach
        void setUp() {
            // 테스트용 회원 등록
            String TEST_NICKNAME = "testUser";

            Member testMember = Member.builder()
                    .email(TEST_EMAIL)
                    .password(passwordEncoder.encode(TEST_PASSWORD))
                    .nickname(TEST_NICKNAME)
                    .role(RoleType.USER)
                    .build();
            memberRepository.save(testMember);
        }

        @Test
        @DisplayName("정상 로그인 성공")
        void loginSuccess() throws Exception {
            // given
            LoginRequestDto requestDto = new LoginRequestDto(TEST_EMAIL, TEST_PASSWORD);

            // when & then
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("로그인 성공"))
                    .andExpect(jsonPath("$.data.memberId").exists())
                    .andExpect(jsonPath("$.data.email").value(TEST_EMAIL))
                    .andExpect(jsonPath("$.data.accessToken").exists())
                    .andExpect(jsonPath("$.data.refreshToken").exists());
        }

        @Test
        @DisplayName("잘못된 비밀번호로 로그인 실패")
        void loginFail_WrongPassword() throws Exception {
            // given
            LoginRequestDto requestDto = new LoginRequestDto(TEST_EMAIL, "wrongpassword");

            // when & then
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("존재하지 않는 이메일로 로그인 실패")
        void loginFail_EmailNotFound() throws Exception {
            // given
            LoginRequestDto requestDto = new LoginRequestDto("notexist@example.com", TEST_PASSWORD);

            // when & then
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("이메일 누락 시 로그인 실패")
        void loginFail_MissingEmail() throws Exception {
            // given
            LoginRequestDto requestDto = new LoginRequestDto(null, TEST_PASSWORD);

            // when & then
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("비밀번호 누락 시 로그인 실패")
        void loginFail_MissingPassword() throws Exception {
            // given
            LoginRequestDto requestDto = new LoginRequestDto(TEST_EMAIL, null);

            // when & then
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("유효하지 않은 이메일 형식으로 로그인 실패")
        void loginFail_InvalidEmailFormat() throws Exception {
            // given
            LoginRequestDto requestDto = new LoginRequestDto("invalid-email-format", TEST_PASSWORD);

            // when & then
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("빈 문자열 이메일로 로그인 실패")
        void loginFail_EmptyEmail() throws Exception {
            // given
            LoginRequestDto requestDto = new LoginRequestDto("", TEST_PASSWORD);

            // when & then
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("빈 문자열 비밀번호로 로그인 실패")
        void loginFail_EmptyPassword() throws Exception {
            // given
            LoginRequestDto requestDto = new LoginRequestDto(TEST_EMAIL, "");

            // when & then
            mockMvc.perform(post("/api/auth/login")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }


}
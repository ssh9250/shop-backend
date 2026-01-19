package com.study.shop.domain.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.shop.ShopApplication;
import com.study.shop.domain.auth.dto.SignupRequestDto;
import com.study.shop.domain.member.entity.Member;
import com.study.shop.domain.member.repository.MemberRepository;
import com.study.shop.global.enums.RoleType;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = ShopApplication.class)
@AutoConfigureMockMvc
@Transactional
class SignupControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;


    @Nested
    @DisplayName("/api/auth/signup - 회원가입")
    class Signup {
        @Test
        @DisplayName("정상 회원가입 - 필수 필드만")
        void signupSuccess_WithRequiredFieldsOnly() throws Exception {
            // given
            SignupRequestDto requestDto = SignupRequestDto.builder()
                    .email("test@example.com")
                    .password("password123")
                    .nickname("테스트유저")
                    .build();

            // when & then
            mockMvc.perform(post("/api/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("회원가입 성공"));

            // 데이터베이스 검증
            Member savedMember = memberRepository.findMemberByEmail("test@example.com").orElseThrow();
            assertThat(savedMember.getEmail()).isEqualTo("test@example.com");
            assertThat(savedMember.getNickname()).isEqualTo("테스트유저");
            assertThat(savedMember.getRole()).isEqualTo(RoleType.USER);
            assertThat(savedMember.getPhone()).isNull();
            assertThat(savedMember.getAddress()).isNull();
            assertThat(passwordEncoder.matches("password123", savedMember.getPassword())).isTrue();
        }

        @Test
        @DisplayName("정상 회원가입 - 모든 필드 포함")
        void signupSuccess_WithAllFields() throws Exception {
            // given
            SignupRequestDto requestDto = SignupRequestDto.builder()
                    .email("test@example.com")
                    .password("password123")
                    .nickname("테스트유저")
                    .phone("010-1234-5678")
                    .address("서울시 강남구")
                    .build();

            // when & then
            mockMvc.perform(post("/api/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andDo(print())
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("회원가입 성공"));

            // 데이터베이스 검증
            Member savedMember = memberRepository.findMemberByEmail("test@example.com").orElseThrow();
            assertThat(savedMember.getEmail()).isEqualTo("test@example.com");
            assertThat(savedMember.getNickname()).isEqualTo("테스트유저");
            assertThat(savedMember.getPhone()).isEqualTo("010-1234-5678");
            assertThat(savedMember.getAddress()).isEqualTo("서울시 강남구");
            assertThat(savedMember.getRole()).isEqualTo(RoleType.USER);
        }

        @Test
        @DisplayName("이메일 중복 시 회원가입 실패")
        void signupFail_DuplicateEmail() throws Exception {
            // given - 기존 회원 등록
            Member existingMember = Member.builder()
                    .email("existing@example.com")
                    .password(passwordEncoder.encode("password123"))
                    .nickname("기존유저")
                    .role(RoleType.USER)
                    .build();
            memberRepository.save(existingMember);

            // when - 동일한 이메일로 가입 시도
            SignupRequestDto requestDto = SignupRequestDto.builder()
                    .email("existing@example.com")
                    .password("newpassword123")
                    .nickname("신규유저")
                    .build();

            // then
            mockMvc.perform(post("/api/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andDo(print())
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").exists());
        }

        @Test
        @DisplayName("닉네임 중복 시 회원가입 실패")
        void signupFail_DuplicateNickname() throws Exception {
            // given - 기존 회원 등록
            Member existingMember = Member.builder()
                    .email("existing@example.com")
                    .password(passwordEncoder.encode("password123"))
                    .nickname("중복닉네임")
                    .role(RoleType.USER)
                    .build();
            memberRepository.save(existingMember);

            // when - 동일한 닉네임으로 가입 시도
            SignupRequestDto requestDto = SignupRequestDto.builder()
                    .email("new@example.com")
                    .password("password123")
                    .nickname("중복닉네임")
                    .build();

            // then
            mockMvc.perform(post("/api/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andDo(print())
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.success").value(false))
                    .andExpect(jsonPath("$.message").exists());
        }

        @Test
        @DisplayName("유효하지 않은 이메일 형식으로 회원가입 실패")
        void signupFail_InvalidEmailFormat() throws Exception {
            // given
            SignupRequestDto requestDto = SignupRequestDto.builder()
                    .email("invalid-email-format")
                    .password("password123")
                    .nickname("테스트유저")
                    .build();

            // when & then
            mockMvc.perform(post("/api/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("비밀번호가 6자 미만일 때 회원가입 실패")
        void signupFail_PasswordTooShort() throws Exception {
            // given
            SignupRequestDto requestDto = SignupRequestDto.builder()
                    .email("test@example.com")
                    .password("12345")  // 5자
                    .nickname("테스트유저")
                    .build();

            // when & then
            mockMvc.perform(post("/api/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("이메일 누락 시 회원가입 실패")
        void signupFail_MissingEmail() throws Exception {
            // given
            SignupRequestDto requestDto = SignupRequestDto.builder()
                    .password("password123")
                    .nickname("테스트유저")
                    .build();

            // when & then
            mockMvc.perform(post("/api/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("비밀번호 누락 시 회원가입 실패")
        void signupFail_MissingPassword() throws Exception {
            // given
            SignupRequestDto requestDto = SignupRequestDto.builder()
                    .email("test@example.com")
                    .nickname("테스트유저")
                    .build();

            // when & then
            mockMvc.perform(post("/api/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("닉네임 누락 시 회원가입 실패")
        void signupFail_MissingNickname() throws Exception {
            // given
            SignupRequestDto requestDto = SignupRequestDto.builder()
                    .email("test@example.com")
                    .password("password123")
                    .build();

            // when & then
            mockMvc.perform(post("/api/auth/signup")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andDo(print())
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }
    }
}
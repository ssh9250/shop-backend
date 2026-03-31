package com.study.shop.domain.member.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.study.shop.common.IntegrationTestBase;
import com.study.shop.domain.auth.dto.LoginRequestDto;
import com.study.shop.domain.member.dto.ChangePasswordRequestDto;
import com.study.shop.domain.member.dto.UpdateProfileRequestDto;
import com.study.shop.domain.member.entity.Member;
import com.study.shop.domain.member.repository.MemberRepository;
import com.study.shop.global.enums.RoleType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

// todo: 이거 보기
@Disabled("수정 중")
class MemberControllerTest extends IntegrationTestBase {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private MemberRepository memberRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private static final String TEST_EMAIL    = "test@example.com";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_NICKNAME = "testUser";

    private Member savedMember;

    @BeforeEach
    void setUp() {
        savedMember = memberRepository.save(Member.builder()
                .email(TEST_EMAIL)
                .password(passwordEncoder.encode(TEST_PASSWORD))
                .nickname(TEST_NICKNAME)
                .phone("010-1234-5678")
                .address("서울시 강남구")
                .role(RoleType.USER)
                .build());
    }

    private String loginAndGetAccessToken() throws Exception {
        LoginRequestDto loginRequest = new LoginRequestDto(TEST_EMAIL, TEST_PASSWORD);
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk())
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.data.accessToken");
    }

    // ─────────────────────────────────────────────
    // GET /api/members/me
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/members/me - 내 정보 조회")
    class GetMe {

        @Test
        @DisplayName("정상 조회 성공 - 응답 필드 검증")
        void getMeSuccess() throws Exception {
            String token = loginAndGetAccessToken();

            mockMvc.perform(get("/api/members/me")
                            .header("Authorization", "Bearer " + token))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").exists())
                    .andExpect(jsonPath("$.data.email").value(TEST_EMAIL))
                    .andExpect(jsonPath("$.data.nickname").value(TEST_NICKNAME))
                    .andExpect(jsonPath("$.data.phone").value("010-1234-5678"))
                    .andExpect(jsonPath("$.data.address").value("서울시 강남구"))
                    .andExpect(jsonPath("$.data.role").value("USER"));
        }

        @Test
        @DisplayName("인증 없이 조회 시 실패")
        void getMeFail_NoAuth() throws Exception {
            mockMvc.perform(get("/api/members/me"))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("유효하지 않은 토큰으로 조회 시 실패")
        void getMeFail_InvalidToken() throws Exception {
            mockMvc.perform(get("/api/members/me")
                            .header("Authorization", "Bearer invalid.token.value"))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }
    }

    // ─────────────────────────────────────────────
    // PATCH /api/members/profile
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("PATCH /api/members/profile - 프로필 수정")
    class UpdateProfile {

        @Test
        @DisplayName("정상 수정 성공 - DB 반영 검증")
        void updateProfileSuccess() throws Exception {
            String token = loginAndGetAccessToken();
            UpdateProfileRequestDto request = new UpdateProfileRequestDto("newNickname", "부산시 해운대구", "010-9999-8888");

            mockMvc.perform(patch("/api/members/profile")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("회원정보 수정 완료"));

            Member updated = memberRepository.findById(savedMember.getId()).orElseThrow();
            assertThat(updated.getNickname()).isEqualTo("newNickname");
            assertThat(updated.getAddress()).isEqualTo("부산시 해운대구");
            assertThat(updated.getPhone()).isEqualTo("010-9999-8888");
        }

        @Test
        @DisplayName("다른 회원의 닉네임으로 수정 시 실패")
        void updateProfileFail_DuplicateNickname() throws Exception {
            memberRepository.save(Member.builder()
                    .email("other@example.com")
                    .password(passwordEncoder.encode("password123"))
                    .nickname("takenNickname")
                    .role(RoleType.USER)
                    .build());

            String token = loginAndGetAccessToken();
            UpdateProfileRequestDto request = new UpdateProfileRequestDto("takenNickname", "서울시 강남구", "010-1234-5678");

            mockMvc.perform(patch("/api/members/profile")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("본인 닉네임 그대로 전달 시 중복으로 처리되어 실패 (known issue)")
        void updateProfileFail_SameNicknameAsOwn() throws Exception {
            // validateDuplicateNickname()이 자기 자신의 닉네임도 중복으로 판단함
            String token = loginAndGetAccessToken();
            UpdateProfileRequestDto request = new UpdateProfileRequestDto(TEST_NICKNAME, "부산시 해운대구", "010-9999-8888");

            mockMvc.perform(patch("/api/members/profile")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("인증 없이 수정 시 실패")
        void updateProfileFail_NoAuth() throws Exception {
            UpdateProfileRequestDto request = new UpdateProfileRequestDto("newNickname", "부산시 해운대구", "010-9999-8888");

            mockMvc.perform(patch("/api/members/profile")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }
    }

    // ─────────────────────────────────────────────
    // PATCH /api/members/password
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("PATCH /api/members/password - 비밀번호 변경")
    class ChangePassword {

        @Test
        @DisplayName("정상 변경 성공 - 새 비밀번호로 암호화 DB 반영 검증")
        void changePasswordSuccess() throws Exception {
            String token = loginAndGetAccessToken();
            ChangePasswordRequestDto request = new ChangePasswordRequestDto("newPassword456");

            mockMvc.perform(patch("/api/members/password")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("비밀번호 수정 완료"));

            Member updated = memberRepository.findById(savedMember.getId()).orElseThrow();
            assertThat(passwordEncoder.matches("newPassword456", updated.getPassword())).isTrue();
            assertThat(passwordEncoder.matches(TEST_PASSWORD, updated.getPassword())).isFalse();
        }

        @Test
        @DisplayName("인증 없이 변경 시 실패")
        void changePasswordFail_NoAuth() throws Exception {
            ChangePasswordRequestDto request = new ChangePasswordRequestDto("newPassword456");

            mockMvc.perform(patch("/api/members/password")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("유효하지 않은 토큰으로 변경 시 실패")
        void changePasswordFail_InvalidToken() throws Exception {
            ChangePasswordRequestDto request = new ChangePasswordRequestDto("newPassword456");

            mockMvc.perform(patch("/api/members/password")
                            .header("Authorization", "Bearer invalid.token.value")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }
    }

    // ─────────────────────────────────────────────
    // DELETE /api/members
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("DELETE /api/members - 회원 탈퇴")
    class DeleteMember {

        @Test
        @DisplayName("정상 탈퇴 성공 - soft delete로 조회 불가 검증")
        void deleteMemberSuccess() throws Exception {
            String token = loginAndGetAccessToken();
            Long memberId = savedMember.getId();

            mockMvc.perform(delete("/api/members")
                            .header("Authorization", "Bearer " + token))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("회원 탈퇴 완료"));

            // @SQLRestriction("deleted_at is NULL") 적용 → 삭제 후 조회 불가
            assertThat(memberRepository.findById(memberId)).isEmpty();
        }

        @Test
        @DisplayName("인증 없이 탈퇴 시 실패")
        void deleteMemberFail_NoAuth() throws Exception {
            mockMvc.perform(delete("/api/members"))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("유효하지 않은 토큰으로 탈퇴 시 실패")
        void deleteMemberFail_InvalidToken() throws Exception {
            mockMvc.perform(delete("/api/members")
                            .header("Authorization", "Bearer invalid.token.value"))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }
    }
}

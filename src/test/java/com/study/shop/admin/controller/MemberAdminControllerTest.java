package com.study.shop.admin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.study.shop.common.IntegrationTestBase;
import com.study.shop.domain.auth.dto.LoginRequestDto;
import com.study.shop.domain.member.entity.Member;
import com.study.shop.domain.member.repository.MemberRepository;
import com.study.shop.global.enums.RoleType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class MemberAdminControllerTest extends IntegrationTestBase {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private MemberRepository memberRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private static final String ADMIN_EMAIL    = "admin@example.com";
    private static final String ADMIN_PASSWORD = "adminpass123";
    private static final String USER_EMAIL     = "user@example.com";
    private static final String USER_PASSWORD  = "userpass123";

    private Member userMember;

    @BeforeEach
    void setUp() {
        memberRepository.save(Member.builder()
                .email(ADMIN_EMAIL)
                .password(passwordEncoder.encode(ADMIN_PASSWORD))
                .nickname("adminUser")
                .address("서울시 강남구")
                .role(RoleType.ADMIN)
                .build());

        userMember = memberRepository.save(Member.builder()
                .email(USER_EMAIL)
                .password(passwordEncoder.encode(USER_PASSWORD))
                .nickname("normalUser")
                .address("서울시 마포구")
                .role(RoleType.USER)
                .build());
    }

    private String loginAndGetToken(String email, String password) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new LoginRequestDto(email, password))))
                .andExpect(status().isOk())
                .andReturn();
        return JsonPath.read(result.getResponse().getContentAsString(), "$.data.accessToken");
    }

    // ─────────────────────────────────────────────
    // GET /admin/member
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("GET /admin/member - 회원 목록 조회")
    class GetMemberList {

        @Test
        @DisplayName("관리자 - 조건 없이 전체 회원 조회 성공")
        void getMemberListSuccess() throws Exception {
            String adminToken = loginAndGetToken(ADMIN_EMAIL, ADMIN_PASSWORD);

            mockMvc.perform(get("/admin/member")
                            .header("Authorization", "Bearer " + adminToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @DisplayName("이메일 조건으로 필터링 조회 성공")
        void getMemberListSuccess_WithEmailFilter() throws Exception {
            String adminToken = loginAndGetToken(ADMIN_EMAIL, ADMIN_PASSWORD);

            mockMvc.perform(get("/admin/member")
                            .param("email", "user")
                            .header("Authorization", "Bearer " + adminToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @DisplayName("역할 조건으로 필터링 조회 성공")
        void getMemberListSuccess_WithRoleFilter() throws Exception {
            String adminToken = loginAndGetToken(ADMIN_EMAIL, ADMIN_PASSWORD);

            mockMvc.perform(get("/admin/member")
                            .param("roleType", "USER")
                            .header("Authorization", "Bearer " + adminToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @DisplayName("일반 사용자 접근 시 403 반환")
        void getMemberListFail_NotAdmin() throws Exception {
            String userToken = loginAndGetToken(USER_EMAIL, USER_PASSWORD);

            mockMvc.perform(get("/admin/member")
                            .header("Authorization", "Bearer " + userToken))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("인증 없이 접근 시 401 반환")
        void getMemberListFail_NoAuth() throws Exception {
            mockMvc.perform(get("/admin/member"))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }
    }

    // ─────────────────────────────────────────────
    // POST /admin/member/kick/{id}
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("POST /admin/member/kick/{id} - 회원 추방")
    class KickMember {

        @Test
        @DisplayName("관리자 - 회원 추방 성공")
        void kickMemberSuccess() throws Exception {
            String adminToken = loginAndGetToken(ADMIN_EMAIL, ADMIN_PASSWORD);

            mockMvc.perform(post("/admin/member/kick/{id}", userMember.getId())
                            .header("Authorization", "Bearer " + adminToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("회원 추방 완료"));
        }

        @Test
        @DisplayName("존재하지 않는 회원 추방 시 404 반환")
        void kickMemberFail_NotFound() throws Exception {
            String adminToken = loginAndGetToken(ADMIN_EMAIL, ADMIN_PASSWORD);

            mockMvc.perform(post("/admin/member/kick/{id}", 99999L)
                            .header("Authorization", "Bearer " + adminToken))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("일반 사용자 접근 시 403 반환")
        void kickMemberFail_NotAdmin() throws Exception {
            String userToken = loginAndGetToken(USER_EMAIL, USER_PASSWORD);

            mockMvc.perform(post("/admin/member/kick/{id}", userMember.getId())
                            .header("Authorization", "Bearer " + userToken))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("인증 없이 접근 시 401 반환")
        void kickMemberFail_NoAuth() throws Exception {
            mockMvc.perform(post("/admin/member/kick/{id}", userMember.getId()))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }
    }
}

package com.study.shop.domain.comment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.study.shop.common.IntegrationTestBase;
import com.study.shop.domain.auth.dto.LoginRequestDto;
import com.study.shop.domain.comment.dto.CreateCommentRequestDto;
import com.study.shop.domain.comment.dto.UpdateCommentRequestDto;
import com.study.shop.domain.comment.entity.Comment;
import com.study.shop.domain.comment.repository.CommentRepository;
import com.study.shop.domain.member.entity.Member;
import com.study.shop.domain.member.repository.MemberRepository;
import com.study.shop.domain.post.entity.Post;
import com.study.shop.domain.post.repository.PostRepository;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CommentControllerTest extends IntegrationTestBase {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private MemberRepository memberRepository;
    @Autowired private PostRepository postRepository;
    @Autowired private CommentRepository commentRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private static final String TEST_EMAIL    = "test@example.com";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_NICKNAME = "testUser";

    private Member savedMember;
    private Post savedPost;

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

        savedPost = postRepository.save(Post.create("testTitle", "testContent", savedMember));
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

    private Comment createTestComment(String content) {
        return commentRepository.save(Comment.create(savedMember, savedPost, content));
    }

    // ─────────────────────────────────────────────
    // POST /api/posts/{postId}/comments
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("POST /api/posts/{postId}/comments - 댓글 생성")
    class CreateComment {

        @Test
        @DisplayName("정상 생성 성공 - 댓글 ID 반환 및 DB 반영 검증")
        void createCommentSuccess() throws Exception {
            String token = loginAndGetAccessToken();
            CreateCommentRequestDto request = new CreateCommentRequestDto("DB 검증 내용");

            MvcResult result = mockMvc.perform(post("/api/posts/{postId}/comments", savedPost.getId())
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isNumber())
                    .andReturn();

            Long commentId = ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.data")).longValue();
            Comment saved = commentRepository.findById(commentId).orElseThrow();
            assertThat(saved.getContent()).isEqualTo("DB 검증 내용");
            assertThat(saved.getPost().getId()).isEqualTo(savedPost.getId());
            assertThat(saved.getMember().getId()).isEqualTo(savedMember.getId());
        }

        @Test
        @DisplayName("내용 빈 값으로 생성 시 400 반환")
        void createCommentFail_BlankContent() throws Exception {
            String token = loginAndGetAccessToken();
            CreateCommentRequestDto request = new CreateCommentRequestDto("");

            mockMvc.perform(post("/api/posts/{postId}/comments", savedPost.getId())
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("존재하지 않는 게시글에 생성 시 404 반환")
        void createCommentFail_PostNotFound() throws Exception {
            String token = loginAndGetAccessToken();
            CreateCommentRequestDto request = new CreateCommentRequestDto("내용");

            mockMvc.perform(post("/api/posts/{postId}/comments", 99999L)
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("인증 없이 생성 시 401 반환")
        void createCommentFail_NoAuth() throws Exception {
            CreateCommentRequestDto request = new CreateCommentRequestDto("내용");

            mockMvc.perform(post("/api/posts/{postId}/comments", savedPost.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }
    }

    // ─────────────────────────────────────────────
    // GET /api/posts/{postId}/comments
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/posts/{postId}/comments - 댓글 조회")
    class GetComment {

        @Test
        @DisplayName("정상 조회 성공 - 댓글 개수 검증")
        void getCommentsSuccess() throws Exception {
            String token = loginAndGetAccessToken();
            createTestComment("댓글1");
            createTestComment("댓글2");
            createTestComment("댓글3");

            mockMvc.perform(get("/api/posts/{postId}/comments", savedPost.getId())
                            .header("Authorization", "Bearer " + token))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(3));
        }

        @Test
        @DisplayName("댓글 없는 게시글 조회 시 빈 배열 반환")
        void getCommentsSuccess_Empty() throws Exception {
            String token = loginAndGetAccessToken();

            mockMvc.perform(get("/api/posts/{postId}/comments", savedPost.getId())
                            .header("Authorization", "Bearer " + token))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data").isEmpty());
        }

        @Test
        @DisplayName("인증 없이 조회 시 401 반환")
        void getCommentsFail_NoAuth() throws Exception {
            mockMvc.perform(get("/api/posts/{postId}/comments", savedPost.getId()))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }
    }

    // ─────────────────────────────────────────────
    // PATCH /api/posts/{postId}/comments/{commentId}
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("PATCH /api/posts/{postId}/comments/{commentId} - 댓글 수정")
    class UpdateComment {

        @Test
        @DisplayName("정상 수정 성공 - DB 반영 검증")
        void updateCommentSuccess() throws Exception {
            String token = loginAndGetAccessToken();
            Comment comment = createTestComment("원래 내용");
            UpdateCommentRequestDto request = new UpdateCommentRequestDto("바뀐 내용");

            mockMvc.perform(patch("/api/posts/{postId}/comments/{commentId}", savedPost.getId(), comment.getId())
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("댓글이 수정되었습니다."));

            Comment updated = commentRepository.findById(comment.getId()).orElseThrow();
            assertThat(updated.getContent()).isEqualTo("바뀐 내용");
        }

        @Test
        @DisplayName("내용 빈 값으로 수정 시 400 반환")
        void updateCommentFail_BlankContent() throws Exception {
            String token = loginAndGetAccessToken();
            Comment comment = createTestComment("원래 내용");
            UpdateCommentRequestDto request = new UpdateCommentRequestDto("");

            mockMvc.perform(patch("/api/posts/{postId}/comments/{commentId}", savedPost.getId(), comment.getId())
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("타인의 댓글 수정 시 403 반환")
        void updateCommentFail_NotOwner() throws Exception {
            Member otherMember = memberRepository.save(Member.builder()
                    .email("other@example.com")
                    .password(passwordEncoder.encode("password123"))
                    .nickname("otherUser")
                    .role(RoleType.USER)
                    .build());
            // commentRepository.save() 없이 getId()를 호출하면 ID가 null → 반드시 저장 필요
            Comment otherComment = commentRepository.save(Comment.create(otherMember, savedPost, "타인의 댓글"));
            String token = loginAndGetAccessToken();

            mockMvc.perform(patch("/api/posts/{postId}/comments/{commentId}", savedPost.getId(), otherComment.getId())
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new UpdateCommentRequestDto("탈취한 댓글"))))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("존재하지 않는 댓글 수정 시 404 반환")
        void updateCommentFail_NotFound() throws Exception {
            String token = loginAndGetAccessToken();

            mockMvc.perform(patch("/api/posts/{postId}/comments/{commentId}", savedPost.getId(), 99999L)
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new UpdateCommentRequestDto("수정 내용"))))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("인증 없이 수정 시 401 반환")
        void updateCommentFail_NoAuth() throws Exception {
            Comment comment = createTestComment("원래 내용");

            mockMvc.perform(patch("/api/posts/{postId}/comments/{commentId}", savedPost.getId(), comment.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(new UpdateCommentRequestDto("수정 내용"))))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }
    }

    // ─────────────────────────────────────────────
    // DELETE /api/posts/{postId}/comments/{commentId}
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("DELETE /api/posts/{postId}/comments/{commentId} - 댓글 삭제")
    class DeleteComment {

        @Test
        @DisplayName("정상 삭제 성공 - soft delete로 조회 불가 검증")
        void deleteCommentSuccess() throws Exception {
            String token = loginAndGetAccessToken();
            Comment comment = createTestComment("삭제 대상 댓글");
            Long commentId = comment.getId();

            mockMvc.perform(delete("/api/posts/{postId}/comments/{commentId}", savedPost.getId(), commentId)
                            .header("Authorization", "Bearer " + token))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("댓글이 삭제되었습니다."));

            // @SQLRestriction("deleted_at is NULL") 적용 → 삭제 후 조회 불가
            assertThat(commentRepository.findById(commentId)).isEmpty();
        }

        @Test
        @DisplayName("타인의 댓글 삭제 시 403 반환")
        void deleteCommentFail_NotOwner() throws Exception {
            Member otherMember = memberRepository.save(Member.builder()
                    .email("other@example.com")
                    .password(passwordEncoder.encode("password123"))
                    .nickname("otherUser")
                    .role(RoleType.USER)
                    .build());
            // commentRepository.save() 없이 getId()를 호출하면 ID가 null → 반드시 저장 필요
            Comment otherComment = commentRepository.save(Comment.create(otherMember, savedPost, "타인의 댓글"));
            String token = loginAndGetAccessToken();

            mockMvc.perform(delete("/api/posts/{postId}/comments/{commentId}", savedPost.getId(), otherComment.getId())
                            .header("Authorization", "Bearer " + token))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("존재하지 않는 댓글 삭제 시 404 반환")
        void deleteCommentFail_NotFound() throws Exception {
            String token = loginAndGetAccessToken();

            mockMvc.perform(delete("/api/posts/{postId}/comments/{commentId}", savedPost.getId(), 99999L)
                            .header("Authorization", "Bearer " + token))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("인증 없이 삭제 시 401 반환")
        void deleteCommentFail_NoAuth() throws Exception {
            Comment comment = createTestComment("삭제 대상 댓글");

            mockMvc.perform(delete("/api/posts/{postId}/comments/{commentId}", savedPost.getId(), comment.getId()))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("게시글 삭제 시 연관된 댓글 cascade 삭제 검증")
        void deleteCommentSuccess_CascadeOnPostDelete() throws Exception {
            String token = loginAndGetAccessToken();
            Comment comment = createTestComment("게시글과 함께 삭제될 댓글");
            Long commentId = comment.getId();

            mockMvc.perform(delete("/api/posts/{postId}", savedPost.getId())
                            .header("Authorization", "Bearer " + token))
                    .andDo(print())
                    .andExpect(status().isOk());

            assertThat(commentRepository.findById(commentId)).isEmpty();
        }

        @Test
        @DisplayName("회원 탈퇴 시 해당 회원의 댓글 조회 불가 검증")
        void deleteCommentSuccess_OnMemberDelete() throws Exception {
            // todo: 현재 soft delete 시 연관 엔티티에 대한 cascade soft delete 미구현 - 추후 재확인 필요
            Member otherMember = memberRepository.save(Member.builder()
                    .email("other@example.com")
                    .password(passwordEncoder.encode("password123"))
                    .nickname("otherUser")
                    .role(RoleType.USER)
                    .build());
            commentRepository.save(Comment.create(otherMember, savedPost, "탈퇴 회원의 댓글"));
            memberRepository.deleteById(otherMember.getId());

            String token = loginAndGetAccessToken();
            mockMvc.perform(get("/api/posts/{postId}/comments", savedPost.getId())
                            .header("Authorization", "Bearer " + token))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data").isEmpty());
        }
    }
}
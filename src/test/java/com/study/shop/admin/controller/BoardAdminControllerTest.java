package com.study.shop.admin.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.study.shop.common.IntegrationTestBase;
import com.study.shop.domain.auth.dto.LoginRequestDto;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BoardAdminControllerTest extends IntegrationTestBase {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private MemberRepository memberRepository;
    @Autowired private PostRepository postRepository;
    @Autowired private CommentRepository commentRepository;
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

    private Post createPost(Member member, String title, boolean hidden) {
        Post post = Post.builder()
                .title(title)
                .content("내용")
                .hidden(hidden)
                .build();
        post.setMember(member);
        return postRepository.save(post);
    }

    private Comment createComment(Member member, Post post, String content) {
        return commentRepository.save(Comment.create(member, post, content));
    }

    // ─────────────────────────────────────────────
    // GET /admin/board/posts
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("GET /admin/board/posts - 숨김 게시글 조회")
    class GetHiddenPosts {

        @Test
        @DisplayName("관리자 - 숨김 게시글 조회 성공")
        void getHiddenPostsSuccess() throws Exception {
            createPost(userMember, "일반 게시글", false);
            createPost(userMember, "숨김 게시글", true);
            String adminToken = loginAndGetToken(ADMIN_EMAIL, ADMIN_PASSWORD);

            mockMvc.perform(get("/admin/board/posts")
                            .header("Authorization", "Bearer " + adminToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }

        @Test
        @DisplayName("게시글 없을 때 빈 배열 반환")
        void getHiddenPosts_Empty() throws Exception {
            String adminToken = loginAndGetToken(ADMIN_EMAIL, ADMIN_PASSWORD);

            mockMvc.perform(get("/admin/board/posts")
                            .header("Authorization", "Bearer " + adminToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(0));
        }

        @Test
        @DisplayName("일반 사용자 접근 시 403 반환")
        void getHiddenPostsFail_NotAdmin() throws Exception {
            String userToken = loginAndGetToken(USER_EMAIL, USER_PASSWORD);

            mockMvc.perform(get("/admin/board/posts")
                            .header("Authorization", "Bearer " + userToken))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("인증 없이 접근 시 401 반환")
        void getHiddenPostsFail_NoAuth() throws Exception {
            mockMvc.perform(get("/admin/board/posts"))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }
    }

    // ─────────────────────────────────────────────
    // GET /admin/board/posts/{postId}
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("GET /admin/board/posts/{postId} - 게시글 단건 조회")
    class GetPostById {

        @Test
        @DisplayName("관리자 - 게시글 단건 조회 성공")
        void getPostByIdSuccess() throws Exception {
            Post post = createPost(userMember, "테스트 게시글", false);
            String adminToken = loginAndGetToken(ADMIN_EMAIL, ADMIN_PASSWORD);

            mockMvc.perform(get("/admin/board/posts/{postId}", post.getId())
                            .header("Authorization", "Bearer " + adminToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.title").value("테스트 게시글"));
        }

        @Test
        @DisplayName("존재하지 않는 게시글 조회 시 404 반환")
        void getPostByIdFail_NotFound() throws Exception {
            String adminToken = loginAndGetToken(ADMIN_EMAIL, ADMIN_PASSWORD);

            mockMvc.perform(get("/admin/board/posts/{postId}", 99999L)
                            .header("Authorization", "Bearer " + adminToken))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("일반 사용자 접근 시 403 반환")
        void getPostByIdFail_NotAdmin() throws Exception {
            Post post = createPost(userMember, "테스트 게시글", false);
            String userToken = loginAndGetToken(USER_EMAIL, USER_PASSWORD);

            mockMvc.perform(get("/admin/board/posts/{postId}", post.getId())
                            .header("Authorization", "Bearer " + userToken))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    // ─────────────────────────────────────────────
    // GET /admin/board/posts/member/{memberId}
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("GET /admin/board/posts/member/{memberId} - 회원별 게시글 조회")
    class GetPostsByMemberId {

        @Test
        @DisplayName("관리자 - 회원별 게시글 조회 성공")
        void getPostsByMemberIdSuccess() throws Exception {
            createPost(userMember, "게시글 1", false);
            createPost(userMember, "게시글 2", false);
            String adminToken = loginAndGetToken(ADMIN_EMAIL, ADMIN_PASSWORD);

            mockMvc.perform(get("/admin/board/posts/member/{memberId}", userMember.getId())
                            .header("Authorization", "Bearer " + adminToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(2));
        }

        @Test
        @DisplayName("게시글 없는 회원 조회 시 빈 배열 반환")
        void getPostsByMemberId_Empty() throws Exception {
            String adminToken = loginAndGetToken(ADMIN_EMAIL, ADMIN_PASSWORD);

            mockMvc.perform(get("/admin/board/posts/member/{memberId}", userMember.getId())
                            .header("Authorization", "Bearer " + adminToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(0));
        }

        @Test
        @DisplayName("일반 사용자 접근 시 403 반환")
        void getPostsByMemberIdFail_NotAdmin() throws Exception {
            String userToken = loginAndGetToken(USER_EMAIL, USER_PASSWORD);

            mockMvc.perform(get("/admin/board/posts/member/{memberId}", userMember.getId())
                            .header("Authorization", "Bearer " + userToken))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    // ─────────────────────────────────────────────
    // DELETE /admin/board/posts/{postId}
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("DELETE /admin/board/posts/{postId} - 게시글 강제 삭제")
    class DeletePost {

        @Test
        @DisplayName("관리자 - 게시글 강제 삭제 성공")
        void deletePostSuccess() throws Exception {
            Post post = createPost(userMember, "삭제할 게시글", false);
            String adminToken = loginAndGetToken(ADMIN_EMAIL, ADMIN_PASSWORD);

            mockMvc.perform(delete("/admin/board/posts/{postId}", post.getId())
                            .header("Authorization", "Bearer " + adminToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("게시글이 삭제되었습니다."));

            assertThat(postRepository.findById(post.getId())).isEmpty();
        }

        @Test
        @DisplayName("존재하지 않는 게시글 삭제 시 404 반환")
        void deletePostFail_NotFound() throws Exception {
            String adminToken = loginAndGetToken(ADMIN_EMAIL, ADMIN_PASSWORD);

            mockMvc.perform(delete("/admin/board/posts/{postId}", 99999L)
                            .header("Authorization", "Bearer " + adminToken))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("일반 사용자 접근 시 403 반환")
        void deletePostFail_NotAdmin() throws Exception {
            Post post = createPost(userMember, "게시글", false);
            String userToken = loginAndGetToken(USER_EMAIL, USER_PASSWORD);

            mockMvc.perform(delete("/admin/board/posts/{postId}", post.getId())
                            .header("Authorization", "Bearer " + userToken))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    // ─────────────────────────────────────────────
    // GET /admin/board/comments
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("GET /admin/board/comments - 전체 댓글 조회")
    class GetAllComments {

        @Test
        @DisplayName("관리자 - 전체 댓글 조회 성공")
        void getAllCommentsSuccess() throws Exception {
            Post post = createPost(userMember, "게시글", false);
            createComment(userMember, post, "댓글 1");
            createComment(userMember, post, "댓글 2");
            String adminToken = loginAndGetToken(ADMIN_EMAIL, ADMIN_PASSWORD);

            mockMvc.perform(get("/admin/board/comments")
                            .header("Authorization", "Bearer " + adminToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(2));
        }

        @Test
        @DisplayName("댓글 없을 때 빈 배열 반환")
        void getAllComments_Empty() throws Exception {
            String adminToken = loginAndGetToken(ADMIN_EMAIL, ADMIN_PASSWORD);

            mockMvc.perform(get("/admin/board/comments")
                            .header("Authorization", "Bearer " + adminToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(0));
        }

        @Test
        @DisplayName("일반 사용자 접근 시 403 반환")
        void getAllCommentsFail_NotAdmin() throws Exception {
            String userToken = loginAndGetToken(USER_EMAIL, USER_PASSWORD);

            mockMvc.perform(get("/admin/board/comments")
                            .header("Authorization", "Bearer " + userToken))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("인증 없이 접근 시 401 반환")
        void getAllCommentsFail_NoAuth() throws Exception {
            mockMvc.perform(get("/admin/board/comments"))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }
    }

    // ─────────────────────────────────────────────
    // GET /admin/board/comments/post/{postId}
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("GET /admin/board/comments/post/{postId} - 게시글별 댓글 조회")
    class GetCommentsByPostId {

        @Test
        @DisplayName("관리자 - 게시글별 댓글 조회 성공")
        void getCommentsByPostIdSuccess() throws Exception {
            Post post = createPost(userMember, "게시글", false);
            createComment(userMember, post, "댓글 내용");
            String adminToken = loginAndGetToken(ADMIN_EMAIL, ADMIN_PASSWORD);

            mockMvc.perform(get("/admin/board/comments/post/{postId}", post.getId())
                            .header("Authorization", "Bearer " + adminToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(1));
        }

        @Test
        @DisplayName("댓글 없는 게시글 조회 시 빈 배열 반환")
        void getCommentsByPostId_Empty() throws Exception {
            Post post = createPost(userMember, "게시글", false);
            String adminToken = loginAndGetToken(ADMIN_EMAIL, ADMIN_PASSWORD);

            mockMvc.perform(get("/admin/board/comments/post/{postId}", post.getId())
                            .header("Authorization", "Bearer " + adminToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(0));
        }

        @Test
        @DisplayName("일반 사용자 접근 시 403 반환")
        void getCommentsByPostIdFail_NotAdmin() throws Exception {
            Post post = createPost(userMember, "게시글", false);
            String userToken = loginAndGetToken(USER_EMAIL, USER_PASSWORD);

            mockMvc.perform(get("/admin/board/comments/post/{postId}", post.getId())
                            .header("Authorization", "Bearer " + userToken))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }

    // ─────────────────────────────────────────────
    // DELETE /admin/board/comments/{commentId}
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("DELETE /admin/board/comments/{commentId} - 댓글 강제 삭제")
    class DeleteComment {

        @Test
        @DisplayName("관리자 - 댓글 강제 삭제 성공")
        void deleteCommentSuccess() throws Exception {
            Post post = createPost(userMember, "게시글", false);
            Comment comment = createComment(userMember, post, "삭제할 댓글");
            String adminToken = loginAndGetToken(ADMIN_EMAIL, ADMIN_PASSWORD);

            mockMvc.perform(delete("/admin/board/comments/{commentId}", comment.getId())
                            .header("Authorization", "Bearer " + adminToken))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("댓글이 삭제되었습니다."));

            assertThat(commentRepository.findById(comment.getId())).isEmpty();
        }

        @Test
        @DisplayName("존재하지 않는 댓글 삭제 시 404 반환")
        void deleteCommentFail_NotFound() throws Exception {
            String adminToken = loginAndGetToken(ADMIN_EMAIL, ADMIN_PASSWORD);

            mockMvc.perform(delete("/admin/board/comments/{commentId}", 99999L)
                            .header("Authorization", "Bearer " + adminToken))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("일반 사용자 접근 시 403 반환")
        void deleteCommentFail_NotAdmin() throws Exception {
            Post post = createPost(userMember, "게시글", false);
            Comment comment = createComment(userMember, post, "댓글");
            String userToken = loginAndGetToken(USER_EMAIL, USER_PASSWORD);

            mockMvc.perform(delete("/admin/board/comments/{commentId}", comment.getId())
                            .header("Authorization", "Bearer " + userToken))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }
    }
}

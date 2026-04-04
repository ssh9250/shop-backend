package com.study.shop.domain.comment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.study.shop.common.IntegrationTestBase;
import com.study.shop.domain.auth.dto.LoginRequestDto;
import com.study.shop.domain.member.entity.Member;
import com.study.shop.domain.member.repository.MemberRepository;
import com.study.shop.domain.post.dto.CreatePostRequestDto;
import com.study.shop.domain.post.entity.Post;
import com.study.shop.domain.post.repository.PostRepository;
import com.study.shop.domain.comment.dto.CreateCommentRequestDto;
import com.study.shop.domain.comment.dto.UpdateCommentRequestDto;
import com.study.shop.domain.comment.entity.Comment;
import com.study.shop.domain.comment.repository.CommentRepository;
import com.study.shop.global.enums.RoleType;
import org.junit.jupiter.api.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockReset;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class CommentControllerTest extends IntegrationTestBase {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private PostRepository postRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private CommentRepository commentRepository;

    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "password123";
    private static final String TEST_NICKNAME = "testUser";

    private static final String TEST_TITLE = "testTitle";
    private static final String TEST_CONTENT = "testContent";

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

        savedPost = postRepository.save(Post.create(
                TEST_TITLE,
                TEST_CONTENT,
                savedMember
        ));
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


    @Nested
    @DisplayName("POST /api/posts/{postId}/comments - 댓글 생성")
    class CreateComment {

        @Test
        @DisplayName("정상 생성 성공 - 댓글 ID 반환 및 DB 반영 검증")
        void createCommentSuccess_DbVerification() throws Exception {
            String token = loginAndGetAccessToken();
            CreateCommentRequestDto request = new CreateCommentRequestDto("DB 검증 내용");

            MvcResult result = mockMvc.perform(post("/api/posts/{postId}/comments", savedPost.getId())
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isNumber())
                    .andReturn();

            Long commentId = ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.data")).longValue();
            Comment saved = commentRepository.findById(commentId).orElse(null);
            assertThat(saved.getContent()).isEqualTo("DB 검증 내용");
            assertThat(saved.getPost().getId()).isEqualTo(savedPost.getId());
            assertThat(saved.getMember().getId()).isEqualTo(savedMember.getId());
        }

        @Test
        @DisplayName("유효하지 않은 게시글에 댓글 작성 불가")
        void createCommentFail_InvalidPost() throws Exception {
            String token = loginAndGetAccessToken();
            CreateCommentRequestDto request = new CreateCommentRequestDto(TEST_CONTENT);

            mockMvc.perform(post("/api/posts/{postId}/comments", 99999L)
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/posts/{postId}/comments - 댓글 조회")
    class GetComment {

        @Test
        @DisplayName("정상 조회 성공")
        void getCommentsSuccess_DbVerification() throws Exception {
            String token = loginAndGetAccessToken();
            createTestComment(TEST_CONTENT + "1");
            createTestComment(TEST_CONTENT + "2");
            createTestComment(TEST_CONTENT + "3");

            mockMvc.perform(get("/api/posts/{postId}/comments", savedPost.getId())
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(3));
        }
    }

    @Nested
    @DisplayName("PATCH /api/posts/{postId}/comments/{id}")
    class UpdateComment {

        @Test
        @DisplayName("정상 수정 성공 - DB 반영 검증")
        void updateCommentSuccess_DbVerification() throws Exception {
            String token = loginAndGetAccessToken();
            Comment comment = createTestComment("원래 내용");
            Long commentId = comment.getId();
            UpdateCommentRequestDto requestDto = new UpdateCommentRequestDto("바뀐 내용");

            MvcResult result = mockMvc.perform(patch("/api/posts/{postId}/comments/{commentId}", savedPost.getId(), comment.getId())
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andReturn();

            Comment saved = commentRepository.findById(commentId).orElse(null);
            assertThat(saved.getContent()).isEqualTo("바뀐 내용");
            assertThat(saved.getPost().getId()).isEqualTo(savedPost.getId());
            assertThat(saved.getMember().getId()).isEqualTo(savedMember.getId());
        }

        @Test
        @DisplayName("타인의 댓글 수정 시 401 반환")
        void updateCommentFail_NotOwner() throws Exception {
            Member otherMember = memberRepository.save(Member.builder()
                    .email("other@example.com")
                    .password(passwordEncoder.encode("password123"))
                    .nickname("otherUser")
                    .role(RoleType.USER)
                    .build());
            Comment otherComment = Comment.create(otherMember, savedPost, "타인의 댓글");
            UpdateCommentRequestDto requestDto = new UpdateCommentRequestDto("탈취한 댓글");
            String token = loginAndGetAccessToken();

            mockMvc.perform(patch("/api/posts/{postId}/comments/{commentId}", savedPost.getId(), otherComment.getId())
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(requestDto)))
                    .andExpect(status().isForbidden());
        }
    }

    @Nested
    @DisplayName("DELETE /api/posts/{postId}/comments/{commentId} - 댓글 삭제")
    class DeleteComment {

        @Test
        @DisplayName("정상 삭제 성공 - soft delete로 조회 불가 검증")
        void deleteCommentSuccess_DbVerification() throws Exception {
            String token = loginAndGetAccessToken();
            Comment comment = createTestComment("삭제 대상 댓글");
            Long commentId = comment.getId();

            mockMvc.perform(delete("/api/posts/{postId}/comments/{commentId}", savedPost.getId(), commentId)
                            .header("Authorization", "Bearer " + token))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("댓글이 삭제되었습니다."));

            assertThat(commentRepository.findById(commentId)).isEmpty();
        }

        @Test
        @DisplayName("게시글 삭제 시 연관된 댓글 삭제")
        void deleteCommentSuccess_DeletePost() throws Exception {
            String token = loginAndGetAccessToken();
            Comment testComment = createTestComment(TEST_CONTENT);
            Long commentId = testComment.getId();

            mockMvc.perform(delete("/api/posts/{postId}", savedPost.getId())
                            .header("Authorization", "Bearer " + token))
                    .andDo(print())
                    .andExpect(status().isOk());

            assertThat(commentRepository.findById(commentId)).isEmpty();
        }

        @Test
        @DisplayName("회원 탈퇴 시 댓글 삭제 검증")
        void deleteCommentSuccess_DeleteMember() throws Exception {
            Member otherMember = memberRepository.save(Member.builder()
                    .email("other@example.com")
                    .password(passwordEncoder.encode("password123"))
                    .nickname("otherUser")
                    .role(RoleType.USER)
                    .build());
            commentRepository.save(Comment.create(otherMember, savedPost, "타인의 댓글"));
            memberRepository.deleteById(otherMember.getId());
            String token = loginAndGetAccessToken();

            mockMvc.perform(get("/api/posts/{postId}/comments", savedPost.getId())
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data").isEmpty());
            // todo: 현재 soft delete 시 연관관계 entity들 모두 soft delete 로직이 없기 때문에 추후 재확인 필요
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
            Comment otherComment = Comment.create(otherMember, savedPost, "타인의 댓글");
            String token = loginAndGetAccessToken();

            mockMvc.perform(delete("/api/posts/{postId}/comments/{commentId}", savedPost.getId(), otherComment.getId())
                            .header("Authorization", "Bearer " + token))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }


    }
}
package com.study.shop.domain.post.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jayway.jsonpath.JsonPath;
import com.study.shop.common.IntegrationTestBase;
import com.study.shop.domain.auth.dto.LoginRequestDto;
import com.study.shop.domain.member.entity.Member;
import com.study.shop.domain.member.repository.MemberRepository;
import com.study.shop.domain.post.dto.CreatePostRequestDto;
import com.study.shop.domain.post.dto.UpdatePostRequestDto;
import com.study.shop.domain.post.entity.Post;
import com.study.shop.domain.post.entity.PostFile;
import com.study.shop.domain.post.repository.PostRepository;
import com.study.shop.global.enums.RoleType;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class PostControllerTest extends IntegrationTestBase {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private MemberRepository memberRepository;
    @Autowired private PostRepository postRepository;
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

    private Post createTestPost(String title, String content) {
        return postRepository.save(Post.create(title, content, savedMember));
    }

    private MockMultipartFile toRequestPart(Object dto) throws Exception {
        return new MockMultipartFile(
                "request", "", MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(dto)
        );
    }

    // ─────────────────────────────────────────────
    // POST /api/posts
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("POST /api/posts - 게시글 생성")
    class CreatePost {

        @Test
        @DisplayName("정상 생성 성공 - 게시글 ID 반환")
        void createPostSuccess() throws Exception {
            String token = loginAndGetAccessToken();
            CreatePostRequestDto request = new CreatePostRequestDto("테스트 제목", "테스트 내용");

            mockMvc.perform(multipart("/api/posts")
                            .file(toRequestPart(request))
                            .header("Authorization", "Bearer " + token))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isNumber());
        }

        @Test
        @DisplayName("정상 생성 성공 - DB 반영 검증")
        void createPostSuccess_DbVerification() throws Exception {
            String token = loginAndGetAccessToken();
            CreatePostRequestDto request = new CreatePostRequestDto("DB 검증 제목", "DB 검증 내용");

            MvcResult result = mockMvc.perform(multipart("/api/posts")
                            .file(toRequestPart(request))
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andReturn();

            Long postId = ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.data")).longValue();
            Post saved = postRepository.findById(postId).orElseThrow();
            assertThat(saved.getTitle()).isEqualTo("DB 검증 제목");
            assertThat(saved.getContent()).isEqualTo("DB 검증 내용");
            assertThat(saved.getMember().getId()).isEqualTo(savedMember.getId());
        }

        @Test
        @DisplayName("제목 빈 값으로 생성 시 400 반환")
        void createPostFail_BlankTitle() throws Exception {
            String token = loginAndGetAccessToken();
            CreatePostRequestDto request = new CreatePostRequestDto("", "테스트 내용");

            mockMvc.perform(multipart("/api/posts")
                            .file(toRequestPart(request))
                            .header("Authorization", "Bearer " + token))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("내용 빈 값으로 생성 시 400 반환")
        void createPostFail_BlankContent() throws Exception {
            String token = loginAndGetAccessToken();
            CreatePostRequestDto request = new CreatePostRequestDto("테스트 제목", "");

            mockMvc.perform(multipart("/api/posts")
                            .file(toRequestPart(request))
                            .header("Authorization", "Bearer " + token))
                    .andDo(print())
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("인증 없이 생성 시 401 반환")
        void createPostFail_NoAuth() throws Exception {
            CreatePostRequestDto request = new CreatePostRequestDto("테스트 제목", "테스트 내용");

            mockMvc.perform(multipart("/api/posts")
                            .file(toRequestPart(request)))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("유효하지 않은 토큰으로 생성 시 401 반환")
        void createPostFail_InvalidToken() throws Exception {
            CreatePostRequestDto request = new CreatePostRequestDto("테스트 제목", "테스트 내용");

            mockMvc.perform(multipart("/api/posts")
                            .file(toRequestPart(request))
                            .header("Authorization", "Bearer invalid.token.value"))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }
    }

    // ─────────────────────────────────────────────
    // GET /api/posts
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/posts - 게시글 목록 조회")
    class SearchPosts {

        @Test
        @DisplayName("전체 조회 성공 - 페이지네이션 응답 검증")
        void searchPostsSuccess_All() throws Exception {
            createTestPost("첫 번째 글", "내용1");
            createTestPost("두 번째 글", "내용2");

            mockMvc.perform(get("/api/posts"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.totalElements").value(2));
        }

        @Test
        @DisplayName("검색 조건 적용 - 모든 분기 커버")
        void searchPostsSuccess_NoCondition() throws Exception {
            createTestPost("Spring Boot 글", "내용");
            createTestPost("Django 글", "내용");
            createTestPost("JavaScript 글", "내용");

            mockMvc.perform(get("/api/posts")
                            .param("title", "J")
                            .param("content", "내용")
                            .param("writer", "test")
                            .param("hidden", "false")
                            .param("from", "2024-01-01T00:00:00")
                            .param("to", "2099-12-31T23:59:59"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content.length()").value(2));
        }

        @Test
        @DisplayName("제목 검색 조건으로 필터링 성공")
        void searchPostsSuccess_ByTitle() throws Exception {
            createTestPost("Spring Boot 글", "내용");
            createTestPost("다른 주제의 글", "내용");

            mockMvc.perform(get("/api/posts")
                            .param("title", "Spring"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content.length()").value(1))
                    .andExpect(jsonPath("$.data.content[0].title").value("Spring Boot 글"));
        }

        @Test
        @DisplayName("게시글이 없을 때 빈 목록 반환")
        void searchPostsSuccess_Empty() throws Exception {
            mockMvc.perform(get("/api/posts"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.totalElements").value(0));
        }

        @Test
        @DisplayName("페이지 크기 파라미터 적용 성공")
        void searchPostsSuccess_WithPageSize() throws Exception {
            for (int i = 1; i <= 5; i++) {
                createTestPost("글 " + i, "내용 " + i);
            }

            mockMvc.perform(get("/api/posts")
                            .param("page", "0")
                            .param("size", "3"))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content.length()").value(3))
                    .andExpect(jsonPath("$.data.totalElements").value(5))
                    .andExpect(jsonPath("$.data.totalPages").value(2));
        }
    }

    // ─────────────────────────────────────────────
    // GET /api/posts/{id}
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("GET /api/posts/{id} - 게시글 단건 조회")
    class GetPost {

        @Test
        @DisplayName("정상 조회 성공 - 응답 필드 검증")
        void getPostSuccess() throws Exception {
            String token = loginAndGetAccessToken();
            Post post = createTestPost("단건 조회 제목", "단건 조회 내용");

            mockMvc.perform(get("/api/posts/{id}", post.getId())
                            .header("Authorization", "Bearer " + token))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(post.getId()))
                    .andExpect(jsonPath("$.data.title").value("단건 조회 제목"))
                    .andExpect(jsonPath("$.data.content").value("단건 조회 내용"))
                    .andExpect(jsonPath("$.data.writer").value(TEST_EMAIL))
                    .andExpect(jsonPath("$.data.viewCount").isNumber())
                    .andExpect(jsonPath("$.data.comments").isArray())
                    .andExpect(jsonPath("$.data.files").isArray());
        }

        @Test
        @DisplayName("조회 시 Redis 조회수 증가 반영 검증")
        void getPostSuccess_ViewCountIncremented() throws Exception {
            String token = loginAndGetAccessToken();
            Post post = createTestPost("조회수 테스트 글", "내용");

            // 동일 게시글 4회 조회
            mockMvc.perform(get("/api/posts/{id}", post.getId())
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk());
            mockMvc.perform(get("/api/posts/{id}", post.getId())
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk());
            mockMvc.perform(get("/api/posts/{id}", post.getId())
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk());

            mockMvc.perform(get("/api/posts/{id}", post.getId())
                            .header("Authorization", "Bearer " + token))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.viewCount").value(3));
            // 해당 로직은 PostController에서 조회수가 후반영 되는 이슈가 있음. 추후 SpringEvents로 재설계 예정
        }

        @Test
        @DisplayName("인증 없이 조회 시 401 반환")
        void getPostFail_NoToken() throws Exception {
            Post post = createTestPost("단건 조회 제목", "단건 조회 내용");

            mockMvc.perform(get("/api/posts/{id}", post.getId()))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("존재하지 않는 게시글 조회 시 404 반환")
        void getPostFail_NotFound() throws Exception {
            String token = loginAndGetAccessToken();

            mockMvc.perform(get("/api/posts/{id}", 99999L)
                            .header("Authorization", "Bearer " + token))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    // ─────────────────────────────────────────────
    // PATCH /api/posts/{id}
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("PATCH /api/posts/{id} - 게시글 수정")
    class UpdatePost {

        @Test
        @DisplayName("정상 수정 성공 - DB 반영 검증")
        void updatePostSuccess() throws Exception {
            String token = loginAndGetAccessToken();
            Post post = createTestPost("원래 제목", "원래 내용");
            UpdatePostRequestDto request = new UpdatePostRequestDto("수정된 제목", "수정된 내용");

            mockMvc.perform(multipart(HttpMethod.PATCH, "/api/posts/{id}", post.getId())
                            .file(toRequestPart(request))
                            .header("Authorization", "Bearer " + token))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("게시글이 수정되었습니다."));

            Post updated = postRepository.findById(post.getId()).orElseThrow();
            assertThat(updated.getTitle()).isEqualTo("수정된 제목");
            assertThat(updated.getContent()).isEqualTo("수정된 내용");
        }

        @Test
        @DisplayName("인증 없이 수정 시 401 반환")
        void updatePostFail_NoAuth() throws Exception {
            Post post = createTestPost("원래 제목", "원래 내용");
            UpdatePostRequestDto request = new UpdatePostRequestDto("수정된 제목", "수정된 내용");

            mockMvc.perform(multipart(HttpMethod.PATCH, "/api/posts/{id}", post.getId())
                            .file(toRequestPart(request)))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("타인의 게시글 수정 시 403 반환")
        void updatePostFail_NotOwner() throws Exception {
            Member otherMember = memberRepository.save(Member.builder()
                    .email("other@example.com")
                    .password(passwordEncoder.encode("password123"))
                    .nickname("otherUser")
                    .role(RoleType.USER)
                    .build());
            Post otherPost = postRepository.save(Post.create("타인의 글", "타인의 내용", otherMember));

            String token = loginAndGetAccessToken();
            UpdatePostRequestDto request = new UpdatePostRequestDto("탈취 제목", "탈취 내용");

            mockMvc.perform(multipart(HttpMethod.PATCH, "/api/posts/{id}", otherPost.getId())
                            .file(toRequestPart(request))
                            .header("Authorization", "Bearer " + token))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("존재하지 않는 게시글 수정 시 404 반환")
        void updatePostFail_NotFound() throws Exception {
            String token = loginAndGetAccessToken();
            UpdatePostRequestDto request = new UpdatePostRequestDto("수정된 제목", "수정된 내용");

            mockMvc.perform(multipart(HttpMethod.PATCH, "/api/posts/{id}", 99999L)
                            .file(toRequestPart(request))
                            .header("Authorization", "Bearer " + token))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    // ─────────────────────────────────────────────
    // DELETE /api/posts/{id}
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("DELETE /api/posts/{id} - 게시글 삭제")
    class DeletePost {

        @Test
        @DisplayName("정상 삭제 성공 - soft delete로 조회 불가 검증")
        void deletePostSuccess() throws Exception {
            String token = loginAndGetAccessToken();
            Post post = createTestPost("삭제 대상 글", "삭제 대상 내용");
            Long postId = post.getId();

            mockMvc.perform(delete("/api/posts/{id}", postId)
                            .header("Authorization", "Bearer " + token))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("게시글이 삭제되었습니다."));

            // @SQLRestriction("deleted_at is NULL") 적용 → 삭제 후 조회 불가
            assertThat(postRepository.findById(postId)).isEmpty();
        }

        @Test
        @DisplayName("인증 없이 삭제 시 401 반환")
        void deletePostFail_NoAuth() throws Exception {
            Post post = createTestPost("삭제 대상 글", "내용");

            mockMvc.perform(delete("/api/posts/{id}", post.getId()))
                    .andDo(print())
                    .andExpect(status().isUnauthorized());
        }

        @Test
        @DisplayName("타인의 게시글 삭제 시 403 반환")
        void deletePostFail_NotOwner() throws Exception {
            Member otherMember = memberRepository.save(Member.builder()
                    .email("other@example.com")
                    .password(passwordEncoder.encode("password123"))
                    .nickname("otherUser")
                    .role(RoleType.USER)
                    .build());
            Post otherPost = postRepository.save(Post.create("타인의 글", "타인의 내용", otherMember));

            String token = loginAndGetAccessToken();

            mockMvc.perform(delete("/api/posts/{id}", otherPost.getId())
                            .header("Authorization", "Bearer " + token))
                    .andDo(print())
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("존재하지 않는 게시글 삭제 시 404 반환")
        void deletePostFail_NotFound() throws Exception {
            String token = loginAndGetAccessToken();

            mockMvc.perform(delete("/api/posts/{id}", 99999L)
                            .header("Authorization", "Bearer " + token))
                    .andDo(print())
                    .andExpect(status().isNotFound());
        }
    }

    // ─────────────────────────────────────────────
    // 파일 첨부 시나리오
    // ─────────────────────────────────────────────
    @Nested
    @DisplayName("파일 첨부 시나리오")
    class PostFileTest {

        private MockMultipartFile mockFile(String filename) {
            return new MockMultipartFile(
                    "files", filename, MediaType.IMAGE_PNG_VALUE,
                    ("dummy-content-" + filename).getBytes()
            );
        }

        private Long createPostWithFile(String token, MockMultipartFile file) throws Exception {
            CreatePostRequestDto request = new CreatePostRequestDto("파일 첨부 글", "내용");
            MvcResult result = mockMvc.perform(multipart("/api/posts")
                            .file(toRequestPart(request))
                            .file(file)
                            .header("Authorization", "Bearer " + token))
                    .andExpect(status().isOk())
                    .andReturn();
            return ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.data")).longValue();
        }

        @Test
        @DisplayName("파일 1개 첨부 생성 성공 - DB에 PostFile 저장 검증")
        void createPost_SingleFile_Saved() throws Exception {
            String token = loginAndGetAccessToken();
            MockMultipartFile file = mockFile("photo.png");
            CreatePostRequestDto request = new CreatePostRequestDto("파일 첨부 글", "내용");

            MvcResult result = mockMvc.perform(multipart("/api/posts")
                            .file(toRequestPart(request))
                            .file(file)
                            .header("Authorization", "Bearer " + token))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andReturn();

            Long postId = ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.data")).longValue();
            Post saved = postRepository.findById(postId).orElseThrow();
            assertThat(saved.getPostFiles()).hasSize(1);
            assertThat(saved.getPostFiles().get(0).getOriginalFileName()).isEqualTo("photo.png");
            assertThat(saved.getPostFiles().get(0).getFileSize()).isEqualTo(file.getSize());
            assertThat(saved.getPostFiles().get(0).getFilePath()).isEqualTo("uploads/posts");
        }

        @Test
        @DisplayName("파일 여러 개 첨부 생성 성공 - PostFile 개수 및 파일명 검증")
        void createPost_MultipleFiles_AllSaved() throws Exception {
            String token = loginAndGetAccessToken();
            CreatePostRequestDto request = new CreatePostRequestDto("멀티 파일 글", "내용");

            MvcResult result = mockMvc.perform(multipart("/api/posts")
                            .file(toRequestPart(request))
                            .file(mockFile("file1.png"))
                            .file(mockFile("file2.png"))
                            .file(mockFile("file3.png"))
                            .header("Authorization", "Bearer " + token))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andReturn();

            Long postId = ((Number) JsonPath.read(result.getResponse().getContentAsString(), "$.data")).longValue();
            Post saved = postRepository.findById(postId).orElseThrow();
            assertThat(saved.getPostFiles()).hasSize(3);
            assertThat(saved.getPostFiles())
                    .extracting(PostFile::getOriginalFileName)
                    .containsExactlyInAnyOrder("file1.png", "file2.png", "file3.png");
        }

        @Test
        @DisplayName("파일 첨부 게시글 단건 조회 - files 응답 필드 검증")
        void getPost_WithFile_ReturnsFileInfo() throws Exception {
            String token = loginAndGetAccessToken();
            MockMultipartFile file = mockFile("sample.png");
            Long postId = createPostWithFile(token, file);

            mockMvc.perform(get("/api/posts/{id}", postId)
                            .header("Authorization", "Bearer " + token))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.files.length()").value(1))
                    .andExpect(jsonPath("$.data.files[0].originalFileName").value("sample.png"))
                    .andExpect(jsonPath("$.data.files[0].filePath").value("uploads/posts"))
                    .andExpect(jsonPath("$.data.files[0].fileSize").value(file.getSize()));
        }

        @Test
        @DisplayName("수정 시 새 파일로 교체 - 기존 파일 삭제 후 새 파일 저장 검증")
        void updatePost_WithNewFile_ReplacesExistingFile() throws Exception {
            String token = loginAndGetAccessToken();
            Long postId = createPostWithFile(token, mockFile("old.png"));

            UpdatePostRequestDto updateRequest = new UpdatePostRequestDto("수정 제목", "수정 내용");
            mockMvc.perform(multipart(HttpMethod.PATCH, "/api/posts/{id}", postId)
                            .file(toRequestPart(updateRequest))
                            .file(mockFile("new.png"))
                            .header("Authorization", "Bearer " + token))
                    .andDo(print())
                    .andExpect(status().isOk());

            Post updated = postRepository.findById(postId).orElseThrow();
            assertThat(updated.getPostFiles()).hasSize(1);
            assertThat(updated.getPostFiles().get(0).getOriginalFileName()).isEqualTo("new.png");
        }

        @Test
        @DisplayName("수정 시 파일 미전송 - 기존 파일 전부 삭제됨")
        void updatePost_WithoutFiles_ClearsExistingFiles() throws Exception {
            // PostService.updatePost()는 항상 기존 파일을 clear한 뒤 새 파일만 추가
            // → files 파라미터 없이 수정하면 기존 파일이 모두 삭제됨
            String token = loginAndGetAccessToken();
            Long postId = createPostWithFile(token, mockFile("existing.png"));

            UpdatePostRequestDto updateRequest = new UpdatePostRequestDto("수정 제목", "수정 내용");
            mockMvc.perform(multipart(HttpMethod.PATCH, "/api/posts/{id}", postId)
                            .file(toRequestPart(updateRequest))
                            .header("Authorization", "Bearer " + token))
                    .andDo(print())
                    .andExpect(status().isOk());

            Post updated = postRepository.findById(postId).orElseThrow();
            assertThat(updated.getPostFiles()).isEmpty();
        }

        @Test
        @DisplayName("파일 첨부 게시글 삭제 - soft delete 및 파일 정리 성공")
        void deletePost_WithFiles_SuccessfullyDeleted() throws Exception {
            String token = loginAndGetAccessToken();
            Long postId = createPostWithFile(token, mockFile("attached.png"));

            mockMvc.perform(delete("/api/posts/{id}", postId)
                            .header("Authorization", "Bearer " + token))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("게시글이 삭제되었습니다."));

            // @SQLRestriction("deleted_at is NULL") 적용 → 삭제 후 조회 불가
            assertThat(postRepository.findById(postId)).isEmpty();
        }
    }
}

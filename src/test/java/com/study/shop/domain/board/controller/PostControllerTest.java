package com.study.shop.domain.board.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.shop.domain.board.dto.CreatePostRequestDto;
import com.study.shop.domain.board.dto.UpdatePostRequestDto;
import com.study.shop.domain.board.entity.Post;
import com.study.shop.domain.board.repository.PostRepository;
import com.study.shop.domain.comment.entity.Comment;
import com.study.shop.domain.comment.repository.CommentRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class PostControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    private final String url = "/api/posts";

    private Long postId;

    private Long commentId;

    @BeforeEach
    void setUp() {
        commentRepository.deleteAll();
        postRepository.deleteAll();

        Post post = Post.builder()
                .title("test title")
                .writer("tester")
                .content("test content")
                .build();

        Comment comment = Comment.builder()
                .post(post)
                .writer("tester")
                .content("test comment")
                .build();

        postId = postRepository.save(post).getId();
        commentId = commentRepository.save(comment).getId();
    }

    @Test
    void 게시글_등록_가능() throws Exception {
        //given
        CreatePostRequestDto requestDto = new CreatePostRequestDto();
        requestDto.setTitle("title");
        requestDto.setContent("content");
        requestDto.setWriter("writer");

        //when
        mockMvc.perform(post(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isNumber());
    }

    @Test
    void 게시글_단건_조회() throws Exception {
        //when
        mockMvc.perform(get(url + "/" + postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postId)))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(postId))
                .andExpect(jsonPath("$.data.title").value("test title"));
    }

    @Test
    void 게시글_목록_조회() throws Exception {
        //given
        Post post = Post.builder()
                .title("new post1")
                .content("aa")
                .writer("tester")
                .build();
        postRepository.save(post);

        //when
        mockMvc.perform(get(url)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postId)))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2));
    }

    @Test
    void 게시글_수정_가능() throws Exception {
        //given
        UpdatePostRequestDto requestDto = new UpdatePostRequestDto();
        requestDto.setTitle("new title");
        requestDto.setContent("new content");

        //when
        mockMvc.perform(patch(url + "/" + postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("게시글이 수정되었습니다."));
    }

    @Test
    void 게시글_삭제_가능() throws Exception {
        //when
        mockMvc.perform(delete(url + "/" + postId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(postId)))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("게시글이 삭제되었습니다."));
    }

    @Test
    void 게시글_삭제_시_댓글도_삭제()  throws Exception {
        //given
        postRepository.deleteById(postId);
        
        //when
        mockMvc.perform(get("/api/comments").param("postId", postId.toString()))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isEmpty());
//                .andDo(print());
    }
}
package com.study.cruisin.domain.comment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.cruisin.domain.board.entity.Post;
import com.study.cruisin.domain.board.repository.PostRepository;
import com.study.cruisin.domain.comment.dto.CreateCommentRequestDto;
import com.study.cruisin.domain.comment.entity.Comment;
import com.study.cruisin.domain.comment.repository.CommentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class CommentControllerTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private CommentRepository commentRepository;

    private Long postId;

    @BeforeEach
    void setUp() {
        commentRepository.deleteAll();
        postRepository.deleteAll();

        Post post = Post.builder()
                .title("테스트 게시글")
                .writer("관리자")
                .content("이것은 내용")
                .build();

        postId = postRepository.save(post).getId();
    }

    @Test
    void 댓글_등록_가능() throws Exception {
        System.out.println("postid : " + postId);
        //given
        CreateCommentRequestDto request = new CreateCommentRequestDto();
        request.setPostId(postId);
        request.setWriter("tiger");
        request.setContent("<UNK> <UNK>");

        //when
        mockMvc.perform(post("/api/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isNumber());
    }

    @Test
    void 게시글_조회시_댓글목록_가져오기() throws Exception {
        // given
        commentRepository.save(
                Comment.builder()
                        .post(postRepository.findById(postId).orElseThrow())
                        .writer("lion")
                        .content("<UNK> <UNK>")
                        .build()
        );

        mockMvc.perform(get("/api/comments")
                        .param("postId", postId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(1));
    }

    @Test
    void 존재하지_않는_게시글에는_댓글등록_불가() throws Exception {
        CreateCommentRequestDto request = new CreateCommentRequestDto();
        request.setPostId(999L);
        request.setWriter("<UNK>");
        request.setContent("<UNK> <UNK>");

        mockMvc.perform(post("/api/comments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("게시글을 찾을 수 없습니다."));
    }
}
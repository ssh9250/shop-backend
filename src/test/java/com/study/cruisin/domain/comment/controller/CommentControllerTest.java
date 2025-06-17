package com.study.cruisin.domain.comment.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.cruisin.domain.board.dto.PostResponseDto;
import com.study.cruisin.domain.board.entity.Post;
import com.study.cruisin.domain.board.repository.PostRepository;
import com.study.cruisin.domain.comment.dto.CreateCommentRequestDto;
import com.study.cruisin.domain.comment.dto.UpdateCommentRequestDto;
import com.study.cruisin.domain.comment.entity.Comment;
import com.study.cruisin.domain.comment.repository.CommentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.util.stream.Collectors;

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

    private static final Logger logger = LoggerFactory.getLogger(CommentControllerTest.class);

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

        //when
        mockMvc.perform(get("/api/comments")
                        .param("postId", postId.toString()))
                //then
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

    @Test
    void 댓글_수정_가능() throws Exception {
        // given
        Comment comment = commentRepository.save(
                Comment.builder()
                        .post(postRepository.findById(postId).orElseThrow())
                        .writer("lion")
                        .content("before update")
                        .build());
        UpdateCommentRequestDto newContent = new UpdateCommentRequestDto();
        newContent.setContent("after update");

        //when
        mockMvc.perform(patch("/api/comments/" + comment.getId())
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(newContent))
                )
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("댓글이 수정되었습니다."));
    }

    @Test
    void 존재하지_않는_댓글_수정_불가() throws Exception {
        //given
        UpdateCommentRequestDto newContent = new UpdateCommentRequestDto();
        newContent.setContent("try update");

        //when
        mockMvc.perform(patch("/api/comments/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newContent))
                )
                //then
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("댓글을 찾을 수 없습니다."));
    }

    @Test
    void 댓글_삭제_가능() throws Exception {
        //given
        Comment comment = commentRepository.save(
                Comment.builder()
                        .post(postRepository.findById(postId).orElseThrow())
                        .writer("lion")
                        .content("before update")
                        .build());

        Comment comment1 = commentRepository.save(
                Comment.builder()
                        .post(postRepository.findById(postId).orElseThrow())
                        .writer("cat")
                        .content("before delete")
                        .build());

        Post post = postRepository.findById(postId).orElseThrow();
        post.addComment(comment);
        post.addComment(comment1);

        logger.debug("삭제전 댓글 목록 : {}", commentRepository.findByPostId(postId));
        logger.debug("삭제전 post가 가진 댓글 목록 : {}",postRepository.findById(postId).orElseThrow().getComments());

        //when
        mockMvc.perform(delete("/api/comments/" + comment.getId()))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("댓글이 삭제되었습니다."));

        logger.info("삭제후 댓글 목록 : {}", commentRepository.findByPostId(postId));
        logger.debug("삭제후 post가 가진 댓글 목록 : {}",
                postRepository.findById(postId).orElseThrow().getComments());
    }

    @Test
    void 존재하지_않는_댓글_삭제_불가() throws Exception {
        //when
        mockMvc.perform(delete("/api/comments/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.message").value("댓글을 찾을 수 없습니다."));
    }
}
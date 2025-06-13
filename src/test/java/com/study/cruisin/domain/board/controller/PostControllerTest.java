package com.study.cruisin.domain.board.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.cruisin.domain.board.dto.CreatePostRequestDto;
import com.study.cruisin.domain.board.entity.Post;
import com.study.cruisin.domain.board.repository.PostRepository;
import com.study.cruisin.domain.comment.entity.Comment;
import com.study.cruisin.domain.comment.repository.CommentRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
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

    private static String url = "/api/posts";

    private Long postId;

    @BeforeEach
    void setUp() {
        commentRepository.deleteAll();
        postRepository.deleteAll();

        Post post = Post.builder()
                .title("테스트 게시글")
                .writer("관리자")
                .content("11")
                .build();

        Comment comment = Comment.builder()
                .post(post)
                .writer("댓글관리자")
                .content("11")
                .build();

        postId = postRepository.save(post).getId();
        commentRepository.save(comment);
    }

    @Test
    @Disabled
    void 게시글_등록_가능() throws Exception {
        //given
        CreatePostRequestDto requestDto = new CreatePostRequestDto();
        requestDto.setTitle("이것은 제목");
        requestDto.setContent("이것은 내용");
        requestDto.setWriter("나는 작성자");

        //when
//        mockMvc.perform()

    }
}
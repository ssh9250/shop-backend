package com.study.shop.domain.member.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.study.shop.domain.post.repository.PostRepository;
import com.study.shop.domain.comment.repository.CommentRepository;
import com.study.shop.domain.member.dto.ChangePasswordRequestDto;
import com.study.shop.domain.member.dto.UpdateProfileRequestDto;
import com.study.shop.domain.member.entity.Member;
import com.study.shop.domain.member.repository.MemberRepository;
import com.study.shop.global.enums.RoleType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static com.study.shop.testutil.MemberTestHelper.createMember;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class MemberControllerTest {
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private CommentRepository commentRepository;
    @Autowired
    private PostRepository postRepository;

    private Long testAdminId;
    private Long testId;

    private static final Logger logger = LoggerFactory.getLogger(MemberControllerTest.class);

    @BeforeEach
    void setUp() {
        memberRepository.deleteAll();
        commentRepository.deleteAll();
        postRepository.deleteAll();

        Member member = createMember("adminEmai@test.coml", "adminPW", "admin", "010-1234-5678", "admin", RoleType.ADMIN);
        Member member1 = createMember("testEmail@test.com", "password", "test1", "010-1234-5678", "tester", RoleType.USER);

        testId = memberRepository.save(member).getId();
    }

//    @Test
//    @Disabled
//    void 회원_생성_가능() throws Exception {
//        //given
//        CreateMemberRequestDto requestDto = new CreateMemberRequestDto();
//        requestDto.setEmail("testEmail@test.com");
//        requestDto.setPassword("testPW");
//        requestDto.setAddress("testAddress");
//        requestDto.setNickname("tester");
//        requestDto.setPhone("010-1234-5678");
//
//
//        //when
//        mockMvc.perform(post("/api/members")
//                        .contentType(MediaType.APPLICATION_JSON)
//                        .content(objectMapper.writeValueAsString(requestDto)))
//                //then
//                .andExpect(status().isOk())
//                .andExpect(jsonPath("$.success").value(true))
//                .andExpect(jsonPath("$.data").isNumber());
//    }

    @Test
    void 회원_정보_조회() throws Exception {
        //given
        Member member = memberRepository.findById(testId).get();

        //when
        mockMvc.perform(get("/api/members/" + testId)
                        .contentType(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value(member.getEmail()));
    }

    @Test
    void 회원_프로필_수정() throws Exception {
        //given
        UpdateProfileRequestDto requestDto = new UpdateProfileRequestDto();
        requestDto.setNickname("updated");
        requestDto.setPhone("010-9999-9999");
        requestDto.setAddress("updated");

        //when
        mockMvc.perform(patch("/api/members/" + testId + "/profile")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("회원정보가 수정되었습니다."));
    }

    @Test
    void 비밀번호_수정() throws Exception {
        //given
        ChangePasswordRequestDto requestDto = new ChangePasswordRequestDto();
        requestDto.setPassword("newPWWWWWWW");

        //when
        mockMvc.perform(patch("/api/members/" + testId + "/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto)))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("비밀번호가 수정되었습니다."));
    }

    @Test
    void 회원_탈퇴() throws Exception {
        //when
        mockMvc.perform(delete("/api/members/" + testId)
                        .contentType(MediaType.APPLICATION_JSON))
                //then
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("회원을 탈퇴하였습니다."));
    }


}
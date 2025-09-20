package com.study.shop.domain.member.service;

import com.study.shop.domain.auth.dto.SignupRequestDto;
import com.study.shop.domain.post.repository.PostRepository;
import com.study.shop.domain.comment.repository.CommentRepository;
import com.study.shop.domain.member.dto.ChangePasswordRequestDto;
import com.study.shop.domain.member.dto.MemberResponseDto;
import com.study.shop.domain.member.dto.UpdateProfileRequestDto;
import com.study.shop.domain.member.entity.Member;
import com.study.shop.domain.member.exception.MemberNotFoundException;
import com.study.shop.domain.member.repository.MemberRepository;
import com.study.shop.global.enums.RoleType;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final PasswordEncoder passwordEncoder;

    public void signup(SignupRequestDto requestDto) {
        // 이메일, 닉네임 등 중복체크 작업 필요
        String encryptedPassword = passwordEncoder.encode(requestDto.getPassword());

        // 빈 문자열을 null로 변환
        String phone = (requestDto.getPhone() != null && !requestDto.getPhone().trim().isEmpty())
                       ? requestDto.getPhone().trim() : null;
        String address = (requestDto.getAddress() != null && !requestDto.getAddress().trim().isEmpty())
                         ? requestDto.getAddress().trim() : null;

        Member member = Member.builder()
                .email(requestDto.getEmail())
                .password(encryptedPassword)
                .nickname(requestDto.getNickname())
                .phone(phone)
                .address(address)
                .role(RoleType.USER)
                .build();
        memberRepository.save(member);
    }

    @Transactional(readOnly = true)
    public MemberResponseDto getMemberById(Long id) {
        return memberRepository.findById(id)
                .map(MemberResponseDto::from)
                .orElseThrow(() -> new MemberNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public MemberResponseDto getMemberByEmail(String email) {
        return memberRepository.findMemberByEmail(email)
                .map(MemberResponseDto::from)
                .orElseThrow(() -> new MemberNotFoundException(email));
    }

    public void updateProfile(Long id, UpdateProfileRequestDto requestDto) {
        Member member = memberRepository.findById(id).orElseThrow(() -> new MemberNotFoundException(id));
        member.updateProfile(requestDto.getNickname(), requestDto.getPhone(), requestDto.getAddress());
    }

    public void updatePassword(Long id, ChangePasswordRequestDto requestDto) {
        Member member = memberRepository.findById(id).orElseThrow(() -> new MemberNotFoundException(id));
        member.updatePassword(requestDto.getPassword());
    }

    public void deleteMember(Long id) {
        Member member = memberRepository.findById(id).orElseThrow(() -> new MemberNotFoundException(id));
        memberRepository.delete(member);
    }
}

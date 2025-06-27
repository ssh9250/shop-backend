package com.study.cruisin.domain.member.service;

import com.study.cruisin.domain.board.repository.PostRepository;
import com.study.cruisin.domain.comment.repository.CommentRepository;
import com.study.cruisin.domain.member.dto.ChangePasswordRequestDto;
import com.study.cruisin.domain.member.dto.CreateMemberRequestDto;
import com.study.cruisin.domain.member.dto.MemberResponseDto;
import com.study.cruisin.domain.member.dto.UpdateProfileRequestDto;
import com.study.cruisin.domain.member.entity.Member;
import com.study.cruisin.domain.member.exception.MemberNotFoundException;
import com.study.cruisin.domain.member.repository.MemberRepository;
import com.study.cruisin.global.enums.RoleType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepository memberRepository;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    public Long createMember(CreateMemberRequestDto requestDto) {
        Member member = Member.builder()
                .email(requestDto.getEmail())
                .password(requestDto.getPassword())
                .nickname(requestDto.getNickname())
                .phone(requestDto.getPhone())
                .address(requestDto.getAddress())
                .role(RoleType.USER)
                .build();
        return memberRepository.save(member).getId();
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

    // 0627 restart ~ 0628
}

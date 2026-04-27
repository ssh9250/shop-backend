package com.study.shop.admin.service;

import com.study.shop.domain.event.MemberWithdrawEvent;
import com.study.shop.domain.member.dto.MemberListResponseDto;
import com.study.shop.domain.member.dto.MemberResponseDto;
import com.study.shop.domain.member.dto.MemberSearchConditionDto;
import com.study.shop.domain.member.entity.Member;
import com.study.shop.domain.member.exception.MemberNotFoundException;
import com.study.shop.domain.member.repository.MemberQueryRepository;
import com.study.shop.domain.member.repository.MemberRepository;
import com.study.shop.domain.member.service.MemberService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class MemberAdminService {
    private final MemberRepository memberRepository;
    private final MemberQueryRepository memberQueryRepository;
    private final MemberService memberService;
    private final ApplicationEventPublisher eventPublisher;

    public List<MemberListResponseDto> memberList(MemberSearchConditionDto cond) {
        return memberQueryRepository.searchMembers(cond);
    }

    public void kickMember(Long memberId) {
        Member member = memberRepository.findById(memberId)
                        .orElseThrow(()->new MemberNotFoundException(memberId));
        member.softDelete();
        eventPublisher.publishEvent(new MemberWithdrawEvent(memberId));
    }
}

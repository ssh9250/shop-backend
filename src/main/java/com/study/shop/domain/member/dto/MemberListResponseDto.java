package com.study.shop.domain.member.dto;

import com.study.shop.global.enums.RoleType;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class MemberListResponseDto {
    private Long email;
    private String nickname;
    private RoleType roleType;
}

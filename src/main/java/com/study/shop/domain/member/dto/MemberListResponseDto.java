package com.study.shop.domain.member.dto;

import com.study.shop.global.enums.RoleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class MemberListResponseDto {
    private String email;
    private String nickname;
    private RoleType roleType;
    private LocalDateTime deletedAt;
}

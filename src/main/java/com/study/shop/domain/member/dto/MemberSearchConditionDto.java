package com.study.shop.domain.member.dto;

import com.querydsl.core.annotations.QueryProjection;
import com.study.shop.global.enums.RoleType;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
//@QueryProjection
public class MemberSearchConditionDto {
    private String email;
    private String nickname;
    private RoleType roleType;
    private Boolean isDeleted;
}

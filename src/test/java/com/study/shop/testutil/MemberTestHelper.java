package com.study.shop.testutil;

import com.study.shop.domain.member.entity.Member;
import com.study.shop.global.enums.RoleType;

public class MemberTestHelper {
    public static Member createMember(String email, String password, String nickname, String phone, String address, RoleType role) {
        return Member.builder()
                .email(email)
                .password(password)
                .nickname(nickname)
                .phone(phone)
                .address(address)
                .role(role)
                .build();
    }
}

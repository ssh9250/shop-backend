package com.study.cruisin.testutil;

import com.study.cruisin.domain.member.entity.Member;
import com.study.cruisin.global.enums.RoleType;

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

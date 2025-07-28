package com.study.shop.domain.member.dto;

import com.study.shop.domain.member.entity.Member;
import com.study.shop.global.enums.RoleType;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MemberResponseDto {
    private Long id;
    private String email;
    private String nickname;
    private String phone;
    private String address;
    private RoleType role;

    public static MemberResponseDto from(Member member) {
        return MemberResponseDto.builder()
                .id(member.getId())
                .email(member.getEmail())
                .nickname(member.getNickname())
                .phone(member.getPhone())
                .address(member.getAddress())
                .role(member.getRole())
                .build();
    }
}

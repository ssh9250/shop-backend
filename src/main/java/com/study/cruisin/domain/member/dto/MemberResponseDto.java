package com.study.cruisin.domain.member.dto;

import com.study.cruisin.domain.member.entity.Member;
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
    private String role;

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

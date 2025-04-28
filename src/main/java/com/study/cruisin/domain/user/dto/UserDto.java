package com.study.cruisin.domain.user.dto;

import lombok.Data;

@Data
public class USerDto {
    private Long id;
    private String username;
    private String teamName;

    public USerDto(Long id, String username, String teamName) {
        this.id = id;
        this.username = username;
        this.teamName = teamName;
    }
}

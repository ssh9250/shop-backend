package com.study.shop.admin.dto;

import java.time.LocalDateTime;

public interface DeletedMemberProjection {
    String getEmail();
    String getNickname();
    String getRole();
    LocalDateTime getDeletedAt();
}
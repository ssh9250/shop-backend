package com.study.shop.domain.post.dto;

import lombok.*;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
// todo: 기본 생성자 없으면 @ModelAttribute는 내부적으로 객체 생성 불가
@AllArgsConstructor
@ToString
public class PostSearchConditionDto {
    private String title;
    private String writer;
    private String content;
    private Boolean hidden;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime from;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime to;
}

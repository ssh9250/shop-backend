package com.study.shop.domain.Item.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
public class ItemSearchConditionDto {
    private String content; // name or description
    private Boolean used;
    private Integer minPrice;
    private Integer maxPrice;
}

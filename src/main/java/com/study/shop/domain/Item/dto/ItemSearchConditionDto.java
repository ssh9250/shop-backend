package com.study.shop.domain.Item.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemSearchConditionDto {
    private String content; // name or description
    private Boolean used;
    private Integer minPrice;
    private Integer maxPrice;
}

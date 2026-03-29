package com.study.shop.domain.Item.dto;

import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@AllArgsConstructor
public class ItemListDto {
    private Long id;
    private String name;
    private int stock;
    private int price;
    private boolean used;
    private String seller;
    private LocalDateTime createTime;
}

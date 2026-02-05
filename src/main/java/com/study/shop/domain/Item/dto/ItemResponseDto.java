package com.study.shop.domain.Item.dto;

import com.study.shop.domain.Item.entity.Item;
import com.study.shop.global.enums.InstrumentCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ItemResponseDto {
    @Schema(description = "상품 id", example = "101")
    private Long id;

    @Schema(description = "상품명", example = "아이바네즈 일렉 기타")
    private String name;

    @Schema(description = "상품 설명", example = "새 상품")
    private String description;

    @Schema(description = "가격(원)", example = "350000", minimum = "0")
    private int price;

    @Schema(description = "중고 여부", example = "true")
    private boolean used;

    @Schema(description = "판매 가능 여부", example = "true")
    private boolean available;

    public static ItemResponseDto from(Item item) {
        return ItemResponseDto.builder()
                .id(item.getId())
                .name(item.getName())
                .description(item.getDescription())
                .price(item.getPrice())
                .used(item.isUsed())
                .available(item.isAvailable())
                .build();
    }
}

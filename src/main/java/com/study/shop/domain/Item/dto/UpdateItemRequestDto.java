package com.study.shop.domain.Item.dto;

import com.study.shop.global.enums.InstrumentCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateItemRequestDto {
    @Schema(description = "수정할 상품명", example = "펜더 일렉 기타")
    private String name;

    @Schema(description = "수정할 브랜드", example = "Fender")
    private String brand;

    @Schema(description = "수정할 상품 설명", example = "1년 사용, 사용감(잔기스) 있음")
    private String description;

    @Schema(description = "수정할 가격(원)", example = "200000", minimum = "0")
    private int price;

    @Schema(description = "중고 여부", example = "true")
    private boolean used;

    @Schema(description = "판매 가능 여부", example = "true")
    private boolean available;

    @Schema(description = "카테고리", example = "ELECTRONIC",
            allowableValues = {"GUITAR","BASS","PIANO","VIOLIN", "DRUM", "WIND", "ELECTRONIC", "ETC"})
    private InstrumentCategory category;
}

package com.study.shop.domain.Item.dto;

import com.study.shop.global.enums.InstrumentCategory;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateItemRequestDto {
    @NotBlank
    @Schema(description = "상품명", example = "아이바네즈 일렉 기타")
    private String name;

    @NotBlank
    @Schema(description = "브랜드", example = "Ibanez")
    private String brand;

    @Schema(description = "상품 설명", example = "새 상품")
    private String description;

    @NotNull
    @PositiveOrZero
    @Schema(description = "가격(원)", example = "350000", minimum = "0")
    private Integer price;

    @NotNull
    @Schema(description = "중고 여부", example = "true")
    private Boolean used;

    @NotNull
    @Schema(description = "판매 가능 여부", example = "true")
    private Boolean available;

    @NotNull
    @Schema(description = "카테고리", example = "GUITAR",
            allowableValues = {"GUITAR","BASS","PIANO","VIOLIN", "DRUM", "WIND", "ELECTRONIC", "ETC"})
    private InstrumentCategory category;

    public boolean isUsed() {
        return used;
    }

    public boolean isAvailable() {
        return false;
    }
}

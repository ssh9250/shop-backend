package com.study.shop.domain.instrument.dto;

import com.study.shop.domain.instrument.entity.Instrument;
import com.study.shop.global.enums.InstrumentCategory;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class InstrumentResponseDto {
    private String name;
    private String brand;
    private String description;
    private int price;
    private boolean used;
    private boolean available;
    private InstrumentCategory category;
    private Long sellerId;

    public static InstrumentResponseDto from(Instrument instrument) {
        return InstrumentResponseDto.builder()
                .name(instrument.getName())
                .brand(instrument.getBrand())
                .description(instrument.getDescription())
                .price(instrument.getPrice())
                .used(instrument.isUsed())
                .available(instrument.isAvailable())
                .category(instrument.getCategory())
                .sellerId(instrument.getSeller().getId())
                .build();
    }
}

package com.study.shop.domain.instrument.dto;

import com.study.shop.global.enums.InstrumentCategory;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateInstrumentRequestDto {
    private String name;
    private String brand;
    private String description;
    private int price;
    private boolean used;
    private boolean available;
    private InstrumentCategory category;
}

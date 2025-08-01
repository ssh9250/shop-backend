package com.study.shop.domain.Instrument.dto;

import com.study.shop.global.enums.InstrumentCategory;
import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateInstrumentRequestDto {
    private String name;
    private String brand;
    private String description;
    private int price;
    private boolean used;
    private boolean available;
    private InstrumentCategory category;
}

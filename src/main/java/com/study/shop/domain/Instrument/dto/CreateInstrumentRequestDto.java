package com.study.shop.domain.Instrument.dto;

import com.study.shop.global.enums.InstrumentCategory;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateInstrumentRequestDto {
    @NotBlank
    private String name;
    @NotBlank
    private String brand;
    @NotBlank
    private String description;
    @NotBlank
    private int price;
    @NotBlank
    private boolean used;
    @NotBlank
    private boolean available;
    @NotBlank
    private InstrumentCategory category;

    /*
    * private Long id;

    private String name;
    private String brand;
    private String description;

    private int price;
    private boolean used;
    private boolean available;

    @Enumerated(EnumType.STRING)
    private InstrumentCategory category;

    @ManyToOne(fetch = FetchType.LAZY)
    private Member seller;
    * */
}

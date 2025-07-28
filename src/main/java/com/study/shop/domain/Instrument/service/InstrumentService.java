package com.study.shop.domain.Instrument.service;

import com.study.shop.domain.Instrument.dto.CreateInstrumentRequestDto;
import com.study.shop.domain.Instrument.entity.Instrument;
import com.study.shop.domain.Instrument.repository.InstrumentRepository;
import jakarta.transaction.Transactional;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
@NoArgsConstructor
@Transactional
public class InstrumentService {
    @Autowired
    private InstrumentRepository instrumentRepository;

    private Long createInstrument(CreateInstrumentRequestDto requestDto) {
        Instrument instrument = Instrument.builder()
                .name(requestDto.getName())
                .brand(requestDto.getBrand())
                .description(requestDto.getDescription())
                .price(requestDto.getPrice())
                .used(requestDto.isUsed())
                .available(requestDto.isAvailable())
                .category(requestDto.getCategory())
                .build();
        return instrumentRepository.save(instrument).getId();
    }
}

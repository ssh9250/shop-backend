package com.study.cruisin.domain.Instrument.service;

import com.study.cruisin.domain.Instrument.dto.CreateInstrumentRequestDto;
import com.study.cruisin.domain.Instrument.repository.InstrumentRepository;
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

    }
}

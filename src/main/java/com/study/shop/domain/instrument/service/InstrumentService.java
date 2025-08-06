package com.study.shop.domain.instrument.service;

import com.study.shop.domain.instrument.dto.CreateInstrumentRequestDto;
import com.study.shop.domain.instrument.dto.InstrumentResponseDto;
import com.study.shop.domain.instrument.dto.UpdateInstrumentRequestDto;
import com.study.shop.domain.instrument.entity.Instrument;
import com.study.shop.domain.instrument.exception.InstrumentNotFoundException;
import com.study.shop.domain.instrument.repository.InstrumentRepository;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@NoArgsConstructor
@Transactional
public class InstrumentService {
    @Autowired
    private InstrumentRepository instrumentRepository;

    public Long createInstrument(CreateInstrumentRequestDto requestDto) {
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

    @Transactional(readOnly = true)
    public List<InstrumentResponseDto> getAllInstruments() {
        return instrumentRepository.findAll().stream()
                .map(InstrumentResponseDto::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public InstrumentResponseDto getInstrumentById(Long id) {
        return instrumentRepository.findById(id)
                .map(InstrumentResponseDto::from)
                .orElseThrow(() -> new InstrumentNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<InstrumentResponseDto> getInstrumentsByMemberId(Long memberId) {
        return instrumentRepository.findBySellerId(memberId).stream()
                .map(InstrumentResponseDto::from)
                .collect(Collectors.toList());
    }

    public void updateInstrument(Long id, UpdateInstrumentRequestDto request) {
        Instrument instrument = instrumentRepository.findById(id)
                .orElseThrow(() -> new InstrumentNotFoundException(id));
        instrument.update(
                request.getName(),
                request.getBrand(),
                request.getDescription(),
                request.getPrice(),
                request.isUsed(),
                request.isAvailable(),
                request.getCategory()
        );
    }

    public void deleteInstrument(Long id) {
        Instrument instrument = instrumentRepository.findById(id)
                .orElseThrow(() -> new InstrumentNotFoundException(id));
        instrumentRepository.deleteById(id);
    }


}

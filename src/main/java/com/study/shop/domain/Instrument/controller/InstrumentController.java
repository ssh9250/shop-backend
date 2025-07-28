package com.study.shop.domain.Instrument.controller;

import com.study.shop.domain.Instrument.service.InstrumentService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/instruments")
public class InstrumentController {
    @Autowired
    private InstrumentService instrumentService;



}

package com.study.shop.domain.instrument.controller;

import com.study.shop.domain.instrument.dto.CreateInstrumentRequestDto;
import com.study.shop.domain.instrument.dto.InstrumentResponseDto;
import com.study.shop.domain.instrument.dto.UpdateInstrumentRequestDto;
import com.study.shop.domain.instrument.service.InstrumentService;
import com.study.shop.global.response.ApiResponse;
import com.study.shop.global.security.auth.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/instruments")
public class InstrumentController {
    private final InstrumentService instrumentService;

    @PostMapping
    public ResponseEntity<ApiResponse<Long>> createInstrument(@RequestBody CreateInstrumentRequestDto request) {
        return ResponseEntity.ok(ApiResponse.success(instrumentService.createInstrument(request)));
    }

    @GetMapping
    public ResponseEntity<ApiResponse<List<InstrumentResponseDto>>> getAllInstruments() {
        return ResponseEntity.ok(ApiResponse.success(instrumentService.getAllInstruments()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InstrumentResponseDto>> getInstrument(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(instrumentService.getInstrumentById(id)));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<InstrumentResponseDto>>> getMyInstruments(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long id = userDetails.getMember().getId();
        return ResponseEntity.ok(ApiResponse.success(instrumentService.getInstrumentsByMemberId(id)));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> updateInstrument(@PathVariable Long id,
                                                              @RequestBody UpdateInstrumentRequestDto request) {
        instrumentService.updateInstrument(id, request);
        return ResponseEntity.ok(ApiResponse.success(null, "악기가 수정되었습니다."));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteInstrument(@PathVariable Long id) {
        instrumentService.deleteInstrument(id);
        return ResponseEntity.ok(ApiResponse.success(null, "악기가 삭제되었습니다."));
    }
}

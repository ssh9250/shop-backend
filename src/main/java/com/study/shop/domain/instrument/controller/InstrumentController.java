package com.study.shop.domain.instrument.controller;

import com.study.shop.domain.instrument.dto.CreateInstrumentRequestDto;
import com.study.shop.domain.instrument.dto.InstrumentResponseDto;
import com.study.shop.domain.instrument.dto.UpdateInstrumentRequestDto;
import com.study.shop.domain.instrument.service.InstrumentService;
import com.study.shop.global.response.ApiResponse;
import com.study.shop.global.security.auth.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/instruments")
@Tag(name = "Instruments", description = "악기 관리 API")
public class InstrumentController {
    private final InstrumentService instrumentService;

    @Operation(summary = "악기 생성", description = "악기를 등록합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "생성 성공")
    @PostMapping
    public ResponseEntity<ApiResponse<Long>> createInstrument(@RequestBody CreateInstrumentRequestDto request) {
        return ResponseEntity.ok(ApiResponse.success(instrumentService.createInstrument(request)));
    }

    @Operation(summary = "악기 단건 조회", description = "id로 특정 악기를 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<InstrumentResponseDto>> getInstrument(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(instrumentService.getInstrumentById(id)));
    }

    @Operation(summary = "내 악기 목록 조회", description = "로그인한 사용자의 악기 목록을 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<InstrumentResponseDto>>> getMyInstruments(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long id = userDetails.getMemberId();
        return ResponseEntity.ok(ApiResponse.success(instrumentService.getInstrumentsByMemberId(id)));
    }

    @Operation(summary = "악기 수정", description = "id로 특정 악기를 수정합니다.")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> updateInstrument(@PathVariable Long id,
                                                              @RequestBody UpdateInstrumentRequestDto request) {
        instrumentService.updateInstrument(id, request);
        return ResponseEntity.ok(ApiResponse.success(null, "악기가 수정되었습니다."));
    }

    @Operation(summary = "악기 삭제", description = "id로 특정 악기를 삭제합니다.")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteInstrument(@PathVariable Long id) {
        instrumentService.deleteInstrument(id);
        return ResponseEntity.ok(ApiResponse.success(null, "악기가 삭제되었습니다."));
    }
}

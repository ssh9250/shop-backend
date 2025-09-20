package com.study.shop.domain.test.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/test")
@Tag(name = "Test", description = "서버 응답 테스트 API")
public class TestController {
    @Operation(summary = "테스트 송신용", description = "테스트를 시작합니다.")
    @GetMapping
    public String createText() {
        return "Test Message";
    }
}

package com.study.shop.domain.Item.controller;

import com.study.shop.domain.Item.dto.CreateItemRequestDto;
import com.study.shop.domain.Item.dto.ItemResponseDto;
import com.study.shop.domain.Item.dto.UpdateItemRequestDto;
import com.study.shop.domain.Item.service.ItemService;
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
@RequestMapping("/api/items")
@Tag(name = "Items", description = "상품 관리 API")
public class ItemController {
    private final ItemService itemService;
    // todo: 리팩토링 및 검증

    @Operation(summary = "상품 생성", description = "상품을 등록합니다.")
    @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "생성 성공")
    @PostMapping
    public ResponseEntity<ApiResponse<Long>> createItems(@RequestBody CreateItemRequestDto request) {
        return ResponseEntity.ok(ApiResponse.success(itemService.createItem(request)));
    }

    @Operation(summary = "상품 단건 조회", description = "id로 특정 상품을 조회합니다.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ItemResponseDto>> getItem(@PathVariable Long id) {
        return ResponseEntity.ok(ApiResponse.success(itemService.getItemById(id)));
    }

    @Operation(summary = "내 상품 목록 조회", description = "로그인한 사용자의 상품 목록을 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<List<ItemResponseDto>>> getMyItems(@AuthenticationPrincipal CustomUserDetails userDetails) {
        Long id = userDetails.getMemberId();
        return ResponseEntity.ok(ApiResponse.success(itemService.getItemsByMemberId(id)));
    }

    @Operation(summary = "상품 수정", description = "id로 특정 상품을 수정합니다.")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> updateItem(@PathVariable Long id,
                                                              @RequestBody UpdateItemRequestDto request) {
        itemService.updateItem(id, request);
        return ResponseEntity.ok(ApiResponse.success(null, "상품이 수정되었습니다."));
    }

    @Operation(summary = "상품 삭제", description = "id로 특정 상품을 삭제합니다.")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteItem(@PathVariable Long id) {
        itemService.deleteItem(id);
        return ResponseEntity.ok(ApiResponse.success(null, "상품이 삭제되었습니다."));
    }
}

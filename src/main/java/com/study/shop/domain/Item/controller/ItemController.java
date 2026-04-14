package com.study.shop.domain.Item.controller;

import com.study.shop.domain.Item.dto.*;
import com.study.shop.domain.Item.service.ItemService;
import com.study.shop.global.response.ApiResponse;
import com.study.shop.global.dto.SliceResponse;
import com.study.shop.security.auth.CustomUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/items")
@Tag(name = "Items", description = "상품 관리 API")
public class ItemController {
    private final ItemService itemService;

    @Operation(summary = "상품 등록", description = "새 상품을 등록합니다. 등록 시 상태는 ON_SALE로 고정됩니다.")
    @PostMapping
    public ResponseEntity<ApiResponse<Long>> createItems(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestBody @Valid CreateItemRequestDto request) {
        return ResponseEntity.ok(ApiResponse.success(itemService.createItem(userDetails.getMemberId(), request)));
    }

    @Operation(summary = "상품 목록 조회", description = """
            커서 기반 페이징으로 상품을 조회합니다. 조건 없이 호출하면 전체 조회입니다.
            - 검색 파라미터: content(상품명/설명), used(중고여부), minPrice, maxPrice
            - 커서 파라미터: lastCreatedAt + lastId (이전 페이지 마지막 항목 값을 넘겨 다음 페이지 조회)
            """)
    @GetMapping
    public ResponseEntity<ApiResponse<SliceResponse<ItemListDto>>> searchItems(
            @ModelAttribute ItemSearchConditionDto conditionDto,
            @Parameter(description = "커서: 이전 페이지 마지막 항목의 createdAt") @RequestParam(required = false) LocalDateTime lastCreatedAt,
            @Parameter(description = "커서: 이전 페이지 마지막 항목의 id") @RequestParam(required = false) Long lastId,
            @PageableDefault(size = 50) Pageable pageable
            ) {
        return ResponseEntity.ok(ApiResponse.success(new SliceResponse<>(itemService.searchItems(conditionDto, lastCreatedAt, lastId, pageable))));
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

    @Operation(summary = "상품 수정", description = "id로 특정 상품을 수정합니다. 판매자 본인만 수정할 수 있습니다.")
    @PutMapping("/{itemId}")
    public ResponseEntity<ApiResponse<Void>> updateItem(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                        @PathVariable Long itemId,
                                                        @RequestBody UpdateItemRequestDto request) {
        itemService.updateItem(userDetails.getMemberId(), itemId, request);
        return ResponseEntity.ok(ApiResponse.success(null, "상품이 수정되었습니다."));
    }

    @Operation(summary = "상품 삭제", description = "id로 특정 상품을 soft delete 처리합니다. 판매자 본인만 삭제할 수 있습니다.")
    @DeleteMapping("/{itemId}")
    public ResponseEntity<ApiResponse<Void>> deleteItem(@AuthenticationPrincipal CustomUserDetails userDetails,
                                                        @PathVariable Long itemId) {
        itemService.deleteItem(userDetails.getMemberId(), itemId);
        return ResponseEntity.ok(ApiResponse.success(null, "상품이 삭제되었습니다."));
    }
}

package com.study.shop.domain.Item.service;

import com.study.shop.domain.Item.dto.*;
import com.study.shop.domain.Item.entity.Item;
import com.study.shop.domain.Item.exception.ItemNotFoundException;
import com.study.shop.domain.Item.repository.ItemRepository;
import com.study.shop.domain.member.entity.Member;
import com.study.shop.domain.member.exception.MemberNotFoundException;
import com.study.shop.domain.member.repository.MemberRepository;
import com.study.shop.domain.order.entity.Order;
import com.study.shop.global.enums.ItemStatus;
import com.study.shop.global.enums.RoleType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ItemService {
    @Autowired
    private final ItemRepository itemRepository;
    @Autowired
    private MemberRepository memberRepository;

    public Long createItem(Long memberId, CreateItemRequestDto requestDto) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));

        Item item = Item.create(member, requestDto.getName(), requestDto.getDescription(), requestDto.getStock(), requestDto.getPrice(), requestDto.getUsed());
        return itemRepository.save(item).getId();
    }

    @Transactional(readOnly = true)
    public List<ItemResponseDto> getAllItems() {
        return itemRepository.findAll().stream()
                .map(ItemResponseDto::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Slice<ItemListDto> searchItems(ItemSearchConditionDto cond, LocalDateTime lastCreatedAt, Long lastId, Pageable pageable) {
        return itemRepository.findByCondition(cond, lastCreatedAt, lastId, pageable);
    }

    @Transactional(readOnly = true)
    public ItemResponseDto getItemById(Long itemId) {
        return itemRepository.findItemByIdWithMember(itemId)
                .map(ItemResponseDto::from)
                .orElseThrow(() -> new ItemNotFoundException(itemId));
    }

    @Transactional(readOnly = true)
    public List<ItemResponseDto> getItemsByMemberId(Long memberId) {
        return itemRepository.findBySellerId(memberId).stream()
                .map(ItemResponseDto::from)
                .collect(Collectors.toList());
    }

    public void updateItem(Long memberId, Long itemId, UpdateItemRequestDto request) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException(itemId));

        validateItemAccess(item, memberId);

        if (!item.getItemStatus().equals(ItemStatus.ON_SALE)) {
            throw new AccessDeniedException("판매중인 상품만 수정할 수 있습니다.");
        }

        item.update(request);
    }

    public void deleteItem(Long memberId, Long itemId) {
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new ItemNotFoundException(itemId));

        validateItemAccess(item, memberId);

        if (!item.getItemStatus().equals(ItemStatus.ON_SALE)) {
            throw new AccessDeniedException("판매중인 상품만 삭제할 수 있습니다.");
        }

        item.softDelete();
        // 상품 조회 시 deleted 된 item은 어떻게?
        // or 연관관계를 맺은 다른 엔티티는?
    }

    private void validateItemAccess(Item item, Long memberId) {
        Member member = memberRepository.findById(memberId)
                .orElseThrow(() -> new MemberNotFoundException(memberId));

        if (!item.getSeller().getId().equals(member.getId())) {
            throw new AccessDeniedException("해당 작업을 수행할 권한이 없습니다.");
        }
    }
}

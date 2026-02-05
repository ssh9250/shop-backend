package com.study.shop.domain.Item.service;

import com.study.shop.domain.Item.dto.CreateItemRequestDto;
import com.study.shop.domain.Item.dto.ItemResponseDto;
import com.study.shop.domain.Item.dto.UpdateItemRequestDto;
import com.study.shop.domain.Item.entity.Item;
import com.study.shop.domain.Item.exception.ItemNotFoundException;
import com.study.shop.domain.Item.repository.ItemRepository;
import com.study.shop.domain.member.entity.Member;
import com.study.shop.domain.member.exception.MemberNotFoundException;
import com.study.shop.domain.member.repository.MemberRepository;
import com.study.shop.global.enums.RoleType;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    public Long createItem(CreateItemRequestDto requestDto) {
        Item item = Item.builder()
                .name(requestDto.getName())
                .description(requestDto.getDescription())
                .price(requestDto.getPrice())
                .used(requestDto.isUsed())
                .available(requestDto.isAvailable())
                .build();
        return itemRepository.save(item).getId();
    }

    @Transactional(readOnly = true)
    public List<ItemResponseDto> getAllItems() {
        return itemRepository.findAll().stream()
                .map(ItemResponseDto::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ItemResponseDto getItemById(Long id) {
        return itemRepository.findById(id)
                .map(ItemResponseDto::from)
                .orElseThrow(() -> new ItemNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<ItemResponseDto> getItemsByMemberId(Long memberId) {
        return itemRepository.findBySellerId(memberId).stream()
                .map(ItemResponseDto::from)
                .collect(Collectors.toList());
    }

    public void updateItem(Long id, UpdateItemRequestDto request, Long userId) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException(id));

        validateItemAccess(item, userId);

        item.update(request);
    }

    public void deleteItem(Long id, Long userId) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException(id));

        validateItemAccess(item, userId);

        itemRepository.deleteById(id);
    }

    private void validateItemAccess(Item item, Long userId) {
        Member member = memberRepository.findById(userId)
                .orElseThrow(() -> new MemberNotFoundException(userId));

        if (!member.getRole().equals(RoleType.ADMIN) && !item.getSeller().getId().equals(member.getId())) {
            throw new AccessDeniedException("상품에 접근할 권한이 없습니다.");
        }
    }
}

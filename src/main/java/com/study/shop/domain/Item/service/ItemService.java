package com.study.shop.domain.Item.service;

import com.study.shop.domain.Item.dto.CreateItemRequestDto;
import com.study.shop.domain.Item.dto.ItemResponseDto;
import com.study.shop.domain.Item.dto.UpdateItemRequestDto;
import com.study.shop.domain.Item.entity.Item;
import com.study.shop.domain.Item.exception.ItemNotFoundException;
import com.study.shop.domain.Item.repository.ItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
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

    public Long createItem(CreateItemRequestDto requestDto) {
        Item item = Item.builder()
                .name(requestDto.getName())
                .brand(requestDto.getBrand())
                .description(requestDto.getDescription())
                .price(requestDto.getPrice())
                .used(requestDto.isUsed())
                .available(requestDto.isAvailable())
                .category(requestDto.getCategory())
                .build();
        return itemRepository.save(item).getId();
    }

    @Transactional(readOnly = true)
    public List<ItemResponseDto> getAllInstruments() {
        return itemRepository.findAll().stream()
                .map(ItemResponseDto::from)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public ItemResponseDto getInstrumentById(Long id) {
        return itemRepository.findById(id)
                .map(ItemResponseDto::from)
                .orElseThrow(() -> new ItemNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public List<ItemResponseDto> getInstrumentsByMemberId(Long memberId) {
        return itemRepository.findBySellerId(memberId).stream()
                .map(ItemResponseDto::from)
                .collect(Collectors.toList());
    }

    public void updateInstrument(Long id, UpdateItemRequestDto request) {
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException(id));
        item.update(
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
        Item item = itemRepository.findById(id)
                .orElseThrow(() -> new ItemNotFoundException(id));
        itemRepository.deleteById(id);
    }


}

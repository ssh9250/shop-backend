package com.study.shop.domain.event;

import com.study.shop.domain.Item.entity.Item;
import com.study.shop.domain.Item.exception.ItemNotFoundException;
import com.study.shop.domain.Item.repository.ItemRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@EnableAsync
@RequiredArgsConstructor
public class OrderAcceptedEventListener {
    private final ItemRepository itemRepository;

    @EventListener
    @Transactional
    public void onOrderAccepted(OrderAcceptedEvent event) {
        Item item = itemRepository.findById(event.itemId()).orElseThrow(() -> new ItemNotFoundException(event.itemId()));
        item.reserveOrSellOut();
    }
}

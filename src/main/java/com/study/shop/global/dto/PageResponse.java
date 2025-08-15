package com.study.shop.global.dto;

import lombok.*;
import org.springframework.data.domain.*;

import java.util.List;

@Getter
@Builder
@AllArgsConstructor
public class PageResponse<T> {
    private final List<T> content;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;
    private final String sort;

    public static <T> PageResponse<T> from(Page<T> page) {
        String sort = page.getSort().isSorted() ? page.getSort().toString() : "";
        return PageResponse.<T>builder()
                .content(page.getContent())
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .sort(sort)
                .build();
    }
}
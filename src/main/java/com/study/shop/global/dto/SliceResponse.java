package com.study.shop.global.dto;

import lombok.Getter;
import org.springframework.data.domain.Slice;

import java.util.List;

@Getter
public class SliceResponse<T> {
    private final List<T> content;
    private final boolean hasNext;
    private final int size;
    private final int number;
    private final int numberOfElements;

    public SliceResponse(Slice<T> slice) {
        this.content = slice.getContent();
        this.hasNext = slice.hasNext();
        this.size = slice.getSize();
        this.number = slice.getNumber();
        this.numberOfElements = slice.getNumberOfElements();
    }
}
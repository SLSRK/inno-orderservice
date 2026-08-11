package com.innowise.orderservice.service;

import com.innowise.orderservice.model.dto.ItemRequestDto;
import com.innowise.orderservice.model.dto.ItemResponseDto;

public interface ItemService {

    ItemResponseDto createItem(ItemRequestDto itemRequestDto);

    ItemResponseDto getItemById(Long id);

    ItemResponseDto updateItem(Long id, ItemRequestDto itemRequestDto);

    ItemResponseDto deleteItem(Long id);
}

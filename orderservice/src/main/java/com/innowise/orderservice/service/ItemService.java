package com.innowise.orderservice.service;

import com.innowise.orderservice.model.dto.ItemRequestDto;
import com.innowise.orderservice.model.dto.ItemResponseDto;

public interface ItemService {

    /**
     * Creates a new item for orders;
     *
     * @param itemRequestDto properties of the new item;
     * @return returns full data of a created item.
     */
    ItemResponseDto createItem(ItemRequestDto itemRequestDto);

    /**
     * Get an item of it exists;
     *
     * @param id the id of item to get;
     * @return returns full data of an item.
     */
    ItemResponseDto getItemById(Long id);

    /**
     * Update an item by id;
     *
     * @param id the id of the item to update;
     * @param itemRequestDto properties of the updated item;
     * @return returns full data of the updated item.
     */
    ItemResponseDto updateItem(Long id, ItemRequestDto itemRequestDto);

    /**
     * Soft delete an item by id;
     *
     * @param id the id of the item to delete;
     * @return returns full data of the deleted item.
     */
    ItemResponseDto deleteItem(Long id);
}

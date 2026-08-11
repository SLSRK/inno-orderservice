package com.innowise.orderservice.service.impl;

import com.innowise.orderservice.exception.NotFoundException;
import com.innowise.orderservice.mapper.ItemMapper;
import com.innowise.orderservice.model.dto.ItemRequestDto;
import com.innowise.orderservice.model.dto.ItemResponseDto;
import com.innowise.orderservice.model.entity.Item;
import com.innowise.orderservice.repository.ItemRepository;
import com.innowise.orderservice.service.ItemService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {

    private final ItemRepository itemRepository;
    private final ItemMapper itemMapper;

    @Transactional
    public ItemResponseDto createItem(ItemRequestDto itemRequestDto) {
        log.debug("Creating an item with name={}, price=${}...",
                itemRequestDto.name(),
                String.format(Locale.US,"%.2f", itemRequestDto.priceInCents() / 100.0));
        return itemMapper
                .toDto(itemRepository
                        .save(itemMapper
                                .toEntity(itemRequestDto)));
    }

    public ItemResponseDto getItemById(Long id) {
        return itemMapper.toDto(itemRepository
                .findByIdAndDeletedFalse(id).orElseThrow(
                        () -> new NotFoundException("Item not found")));
    }

    @Transactional
    public ItemResponseDto updateItem(Long id, ItemRequestDto itemRequestDto) {
        log.debug("Updating an item with the id={} with: name={}, price=${}...",
                id,
                itemRequestDto.name(),
                String.format(Locale.US,"%.2f", itemRequestDto.priceInCents() / 100.0));
        Item item = itemRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Item not found"));

        item.setName(itemRequestDto.name());
        item.setPrice(itemRequestDto.priceInCents());

        return itemMapper
                .toDto(itemRepository.save(item));
    }

    @Transactional
    public ItemResponseDto deleteItem(Long id) {
        log.debug("Deleting an item with the id={}...", id);
        Item item = itemRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Item not found"));
        item.setDeleted(true);
        return  itemMapper
                .toDto(itemRepository.save(item));
    }
}

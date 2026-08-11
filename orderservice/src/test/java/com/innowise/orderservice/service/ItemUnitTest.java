package com.innowise.orderservice.service;

import com.innowise.orderservice.exception.NotFoundException;
import com.innowise.orderservice.mapper.ItemMapper;
import com.innowise.orderservice.model.dto.ItemRequestDto;
import com.innowise.orderservice.model.dto.ItemResponseDto;
import com.innowise.orderservice.model.entity.Item;
import com.innowise.orderservice.repository.ItemRepository;
import com.innowise.orderservice.service.impl.ItemServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ItemUnitTest {
    @Mock
    private ItemRepository itemRepository;

    @Mock
    private ItemMapper itemMapper;

    @InjectMocks
    private ItemServiceImpl itemService;

    @Test
    void createItem_shouldCreateItem() {
        ItemRequestDto dto = Mockito.mock(ItemRequestDto.class);
        Item item = testItem();
        Item savedItem = testItem();
        ItemResponseDto itemResponseDto = Mockito.mock(ItemResponseDto.class);

        when(dto.name()).thenReturn("Milk");
        when(dto.priceInCents()).thenReturn(900L);
        when(itemMapper.toEntity(dto)).thenReturn(item);
        when(itemRepository.save(item)).thenReturn(savedItem);
        when(itemMapper.toDto(savedItem)).thenReturn(itemResponseDto);
        ItemResponseDto result = itemService.createItem(dto);

        assertEquals(itemResponseDto, result);
        verify(itemRepository).save(item);
    }

    @Test
    void getItemById_shouldReturnItem() {
        Item item = testItem();
        ItemResponseDto itemResponseDto = Mockito.mock(ItemResponseDto.class);

        when(itemRepository.findByIdAndDeletedFalse(1L))
                .thenReturn(Optional.of(item));
        when(itemMapper.toDto(item))
                .thenReturn(itemResponseDto);
        ItemResponseDto result = itemService.getItemById(1L);
        assertEquals(itemResponseDto, result);
    }

    @Test
    void getItemById_shouldThrowWhenNotFound() {
        when(itemRepository.findByIdAndDeletedFalse(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> itemService.getItemById(1L)
        );
    }

    @Test
    void updateItem_shouldUpdateFields() {
        Item item = testItem();
        ItemRequestDto itemRequestDto = Mockito.mock(ItemRequestDto.class);
        ItemResponseDto itemResponseDto = Mockito.mock(ItemResponseDto.class);

        when(itemRequestDto.name()).thenReturn("Bread");
        when(itemRequestDto.priceInCents()).thenReturn(250L);
        when(itemRepository.findByIdAndDeletedFalse(1L))
                .thenReturn(Optional.of(item));
        when(itemRepository.save(item))
                .thenReturn(item);
        when(itemMapper.toDto(item))
                .thenReturn(itemResponseDto);
        ItemResponseDto result = itemService.updateItem(1L, itemRequestDto);

        assertEquals(itemResponseDto, result);
        assertEquals("Bread", item.getName());
        assertEquals(250L, item.getPrice());
        verify(itemRepository).save(item);
    }

    @Test
    void updateItem_shouldThrowWhenNotFound() {
        ItemRequestDto itemRequestDto = Mockito.mock(ItemRequestDto.class);

        when(itemRepository.findByIdAndDeletedFalse(1L))
                .thenReturn(Optional.empty());
        assertThrows(
                NotFoundException.class,
                () -> itemService.updateItem(1L, itemRequestDto)
        );
    }

    @Test
    void deleteItem_shouldMarkItemDeleted() {
        Item item = testItem();
        ItemResponseDto itemResponseDto = Mockito.mock(ItemResponseDto.class);

        when(itemRepository.findByIdAndDeletedFalse(1L))
                .thenReturn(Optional.of(item));
        when(itemRepository.save(item))
                .thenReturn(item);
        when(itemMapper.toDto(item))
                .thenReturn(itemResponseDto);
        ItemResponseDto result = itemService.deleteItem(1L);

        assertEquals(itemResponseDto, result);
        assertTrue(item.getDeleted());
        verify(itemRepository).save(item);
    }

    @Test
    void deleteItem_shouldThrowWhenNotFound() {
        when(itemRepository.findByIdAndDeletedFalse(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> itemService.deleteItem(1L)
        );
    }

    private Item testItem() {
        Item item = new Item();
        item.setId(1L);
        item.setName("Milk");
        item.setPrice(900L);
        item.setDeleted(false);
        return item;
    }
}

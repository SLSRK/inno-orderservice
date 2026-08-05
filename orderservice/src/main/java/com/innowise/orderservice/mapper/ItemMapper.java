package com.innowise.orderservice.mapper;

import com.innowise.orderservice.model.dto.ItemRequestDto;
import com.innowise.orderservice.model.dto.ItemResponseDto;
import com.innowise.orderservice.model.entity.Item;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface ItemMapper {

    ItemResponseDto toDto(Item item);

    Item toEntity(ItemRequestDto itemRequestDto);
}

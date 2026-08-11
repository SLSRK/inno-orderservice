package com.innowise.orderservice.mapper;

import com.innowise.orderservice.model.dto.ItemRequestDto;
import com.innowise.orderservice.model.dto.ItemResponseDto;
import com.innowise.orderservice.model.entity.Item;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Locale;

@Mapper(componentModel = "spring")
public interface ItemMapper {

    @Mapping(target = "price", expression = "java(mapPrice(item.getPrice()))")
    ItemResponseDto toDto(Item item);

    @Mapping(target = "price", source = "priceInCents")
    @Mapping(target = "deleted", constant = "false")
    Item toEntity(ItemRequestDto itemRequestDto);

    default String mapPrice(Long priceInCents) {
        return String.format(Locale.US,"%.2f", priceInCents / 100.0);
    }
}

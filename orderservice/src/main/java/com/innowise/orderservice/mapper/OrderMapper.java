package com.innowise.orderservice.mapper;

import com.innowise.orderservice.model.dto.OrderCreateDto;
import com.innowise.orderservice.model.dto.OrderResponseDto;
import com.innowise.orderservice.model.dto.OrderUpdateDto;
import com.innowise.orderservice.model.entity.Order;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Locale;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "totalPrice", ignore = true)
    @Mapping(target = "deleted", constant = "false")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "orderItems", expression = "java(dto.orderItems().stream().map(i -> {" +
            "OrderItem item = new OrderItem();" +
            "item.setQuantity(i.quantity());" +
            "return item;" +
            "}).toList())")
    Order toEntity(OrderCreateDto dto);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "status", expression = "java(OrderStatus.valueOf(dto.status()))")
    @Mapping(target = "totalPrice", ignore = true)
    @Mapping(target = "deleted", constant = "false")
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "orderItems", expression = "java(dto.orderItems().stream().map(i -> {" +
            "OrderItem item = new OrderItem();" +
            "item.setQuantity(i.quantity());" +
            "return item;" +
            "}).toList())")
    Order toEntity(OrderUpdateDto dto);

    @Mapping(target = "totalPrice", expression = "java(mapPrice(order.getTotalPrice()))")
    OrderResponseDto toDto(Order order);

    default String mapPrice(Long priceInCents) {
        return String.format(Locale.US,"%.2f", priceInCents / 100.0);
    }
}

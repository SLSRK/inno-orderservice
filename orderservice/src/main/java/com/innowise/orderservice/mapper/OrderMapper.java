package com.innowise.orderservice.mapper;

import com.innowise.orderservice.model.dto.OrderCreateDto;
import com.innowise.orderservice.model.dto.OrderItemResponseDto;
import com.innowise.orderservice.model.dto.OrderResponseDto;
import com.innowise.orderservice.model.dto.OrderUpdateDto;
import com.innowise.orderservice.model.entity.Order;
import com.innowise.orderservice.model.entity.OrderItem;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import java.util.Locale;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    @Mapping(target = "status", constant = "PENDING")
    @Mapping(target = "deleted", constant = "false")
    @Mapping(target = "orderItems", expression = "java(dto.orderItems().stream().map(i -> {" +
            "OrderItem item = new OrderItem();" +
            "item.setQuantity(i.quantity());" +
            "return item;" +
            "}).toList())")
    Order toEntity(OrderCreateDto dto);

    @Mapping(target = "status", expression = "java(OrderStatus.valueOf(dto.status()))")
    @Mapping(target = "deleted", constant = "false")
    @Mapping(target = "orderItems", expression = "java(dto.orderItems().stream().map(i -> {" +
            "OrderItem item = new OrderItem();" +
            "item.setQuantity(i.quantity());" +
            "return item;" +
            "}).toList())")
    Order toEntity(OrderUpdateDto dto);

    @Mapping(target = "totalPrice", expression = "java(mapPrice(order.getTotalPrice()))")
    OrderResponseDto toDto(Order order);

    @Mapping(source = "order.id", target = "orderId")
    OrderItemResponseDto toDto(OrderItem orderItem);

    default String mapPrice(Long priceInCents) {
        return String.format(Locale.US,"%.2f", priceInCents / 100.0);
    }
}

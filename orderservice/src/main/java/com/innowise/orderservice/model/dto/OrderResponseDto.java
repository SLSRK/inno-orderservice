package com.innowise.orderservice.model.dto;

import java.time.LocalDateTime;
import java.util.List;

public record OrderResponseDto(

        Long id,

        Long userId,

        String status,

        String totalPrice,

        Boolean deleted,

        LocalDateTime createdAt,

        LocalDateTime updatedAt,

        List<OrderItemResponseDto> orderItems,

        UserResponseDto user
) {
}

package com.innowise.orderservice.model.dto;

import java.time.LocalDateTime;

public record OrderItemResponseDto(

        Long id,

        Long orderId,

        ItemResponseDto item,

        Long quantity,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {
}

package com.innowise.orderservice.model.dto;

import java.io.Serializable;
import java.time.LocalDateTime;

public record OrderItemResponseDto(

        Long id,

        Long orderId,

        ItemResponseDto item,

        Long quantity,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) implements Serializable {
    private static final long serialVersionUID = 1L;
}

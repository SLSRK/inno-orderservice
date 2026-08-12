package com.innowise.orderservice.model.dto;

import lombok.Builder;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.List;

@Builder
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
) implements Serializable {
    private static final long serialVersionUID = 1L;
}

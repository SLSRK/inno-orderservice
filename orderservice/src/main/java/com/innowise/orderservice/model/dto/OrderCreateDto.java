package com.innowise.orderservice.model.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OrderCreateDto(

        @NotNull(message = "User ID cannot be null")
        Long userId,

        @NotEmpty(message = "Order must contain at least 1 item")
        List<OrderItemRequestDto> orderItems
) {
}

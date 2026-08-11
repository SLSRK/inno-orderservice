package com.innowise.orderservice.model.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.util.List;

public record OrderUpdateDto(

        @NotNull(message = "User ID cannot be null")
        Long userId,

        @NotNull(message = "Status cannot be empty")
        String status,

        @NotEmpty(message = "Order must contain at least 1 item")
        List<OrderItemRequestDto> orderItems
) {
}

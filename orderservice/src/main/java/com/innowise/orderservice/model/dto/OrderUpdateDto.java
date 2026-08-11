package com.innowise.orderservice.model.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.List;

public record OrderUpdateDto(

        @NotNull(message = "User ID cannot be null")
        Long userId,

        @NotNull(message = "Status cannot be empty")
        @Pattern(
                regexp = "PENDING|PAID|IN_DELIVERY|DELIVERED|CANCELLED",
                message = "Role can be PENDING, PAID, IN_DELIVERY, DELIVERED or CANCELLED"
        )
        String status,

        @NotEmpty(message = "Order must contain at least 1 item")
        List<OrderItemRequestDto> orderItems
) {
}

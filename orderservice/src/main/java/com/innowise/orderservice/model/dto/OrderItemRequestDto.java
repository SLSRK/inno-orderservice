package com.innowise.orderservice.model.dto;

import jakarta.validation.constraints.NotNull;

public record OrderItemRequestDto(

        @NotNull(message = "Item id cannot be null")
        Long itemId,

        @NotNull(message = "Quantity cannot be null")
        Long quantity
) {
}

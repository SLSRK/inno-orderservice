package com.innowise.orderservice.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;

public record ItemRequestDto(

        @NotBlank(message = "Name cannot be empty")
        @Size(max = 100, message = "Item name must be no longer than 100 characters")
        String name,

        @NotNull(message = "Price cannot be null")
        @PositiveOrZero
        Long priceInCents
) {
}

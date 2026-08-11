package com.innowise.orderservice.model.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record ItemRequestDto(

        @NotBlank(message = "Name cannot be empty")
        String name,

        @NotNull(message = "Price cannot be null")
        @PositiveOrZero
        Long priceInCents
) {
}

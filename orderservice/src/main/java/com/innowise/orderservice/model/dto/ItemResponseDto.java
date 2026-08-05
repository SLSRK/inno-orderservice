package com.innowise.orderservice.model.dto;

import java.time.LocalDateTime;

public record ItemResponseDto(

        Long id,

        String name,

        String price,

        Boolean deleted,

        LocalDateTime createdAt,

        LocalDateTime updatedAt
) {

}

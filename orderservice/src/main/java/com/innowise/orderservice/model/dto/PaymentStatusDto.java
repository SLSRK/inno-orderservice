package com.innowise.orderservice.model.dto;

public record PaymentStatusDto(

        Long orderId,

        String status
) {
}

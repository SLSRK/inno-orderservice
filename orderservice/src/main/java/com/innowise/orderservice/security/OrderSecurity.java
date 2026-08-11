package com.innowise.orderservice.security;

import com.innowise.orderservice.repository.OrderRepository;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
public class OrderSecurity {

    private final OrderRepository orderRepository;

    public boolean isOrderer(Long orderId, Long userId) {
        return orderRepository.findById(orderId)
                .map(order -> order.getUserId().equals(userId))
                .orElse(false);
    }
}

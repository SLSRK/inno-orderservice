package com.innowise.orderservice.service.kafka;

import com.innowise.orderservice.model.dto.PaymentStatusDto;
import com.innowise.orderservice.model.entity.OrderStatus;
import com.innowise.orderservice.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class KafkaConsumerService {

    private final OrderService orderService;

    @KafkaListener(
            topics = "payment.cdc.status-changed",
            containerFactory = "kafkaListenerContainerFactory"
    )
    public void consume(PaymentStatusDto message) {
        if ("SUCCESS".equals(message.status())) {
            orderService.setStatus(message.orderId(), OrderStatus.PAID);
        }
    }
}

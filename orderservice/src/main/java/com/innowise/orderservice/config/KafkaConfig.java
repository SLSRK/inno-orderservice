package com.innowise.orderservice.config;

import com.innowise.orderservice.model.dto.PaymentStatusDto;
import org.springframework.boot.kafka.autoconfigure.KafkaProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.kafka.config.ConcurrentKafkaListenerContainerFactory;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;

@Configuration
@EnableKafka
public class KafkaConfig {

    @Bean
    public ConsumerFactory<String, PaymentStatusDto> consumerFactory(KafkaProperties props) {
        return new DefaultKafkaConsumerFactory<>(
                props.buildConsumerProperties()
        );
    }

    @Bean
    public ConcurrentKafkaListenerContainerFactory<String, PaymentStatusDto> kafkaListenerContainerFactory(
            ConsumerFactory<String, PaymentStatusDto> cf
    ) {
        ConcurrentKafkaListenerContainerFactory<String, PaymentStatusDto> factory =
                new ConcurrentKafkaListenerContainerFactory<>();

        factory.setConsumerFactory(cf);
        return factory;
    }
}
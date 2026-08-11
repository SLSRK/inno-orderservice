package com.innowise.orderservice.config;

import com.innowise.orderservice.security.AuthInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

import java.time.Duration;

@Configuration
@RequiredArgsConstructor
public class RestConfig {

    @Value("${user.service.url}")
    private String userServiceUrl;

    private final AuthInterceptor authInterceptor;

    @Bean
    public RestTemplate restTemplate(RestTemplateBuilder builder) {
        return builder
                .connectTimeout(Duration.ofSeconds(3))
                .readTimeout(Duration.ofSeconds(5))
                .interceptors(authInterceptor)
                .baseUri(userServiceUrl)
                .build();
    }
}

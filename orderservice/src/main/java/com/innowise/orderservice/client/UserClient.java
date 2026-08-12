package com.innowise.orderservice.client;

import com.innowise.orderservice.exception.ForeignServiceException;
import com.innowise.orderservice.model.dto.OrderResponseDto;
import com.innowise.orderservice.model.dto.UserResponseDto;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class UserClient {

    private final RestTemplate restTemplate;

    @CircuitBreaker(name = "userService", fallbackMethod = "getUserByIdFallback")
    public UserResponseDto getUserById(Long id) {
        return restTemplate.getForObject(
                "/api/users/{id}",
                UserResponseDto.class,
                id
        );
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "getUsersByIdsFallback")
    public List<UserResponseDto> getUsersByIds(List<Long> ids) {
        String idsParam = ids.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        return Arrays.asList(restTemplate.getForEntity(
                "/api/users/batch?ids={ids}",
                UserResponseDto[].class,
                idsParam).getBody());
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "getUserByEmailFallback")
    public UserResponseDto getUserByEmail(String email) {
        return restTemplate.getForObject(
                "/api/users/email/{email}",
                UserResponseDto.class,
                email
        );
    }

    private UserResponseDto getUserByIdFallback(Long id, Throwable throwable) {
        return new UserResponseDto(
                id,
                "Unknown",
                "Unknown",
                null,
                null,
                "Unknown",
                null,
                null
        );
    }

    private List<UserResponseDto> getUsersByIdsFallback(List<Long> ids, Throwable throwable) {
        return ids.stream()
                .map(id -> new UserResponseDto(
                        id,
                        "Unknown",
                        "Unknown",
                        null,
                        null,
                        "Unknown",
                        null,
                        null
                ))
                .toList();
    }

    private UserResponseDto getUserByEmailFallback(String email, Throwable throwable) {
        throw new ForeignServiceException("UserService is unavailable", throwable);
    }
}

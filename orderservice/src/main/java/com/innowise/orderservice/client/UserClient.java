package com.innowise.orderservice.client;

import com.innowise.orderservice.exception.ForeignServiceException;
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

    private static final String URI = "/api/v1/users";
    private static final String UNKNOWN = "Unknown";

    @CircuitBreaker(name = "userService", fallbackMethod = "getUserByIdFallback")
    public UserResponseDto getUserById(Long id) {
        return restTemplate.getForObject(
                URI + "/{id}",
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
                URI + "/batch?ids={ids}",
                UserResponseDto[].class,
                idsParam).getBody());
    }

    @CircuitBreaker(name = "userService", fallbackMethod = "getUserByEmailFallback")
    public UserResponseDto getUserByEmail(String email) {
        return restTemplate.getForObject(
                URI + "/email/{email}",
                UserResponseDto.class,
                email
        );
    }

    private UserResponseDto getUserByIdFallback(Long id, Throwable throwable) {
        return UserResponseDto.builder()
                .id(id)
                .name(UNKNOWN)
                .surname(UNKNOWN)
                .email(UNKNOWN)
                .build();
    }

    private List<UserResponseDto> getUsersByIdsFallback(List<Long> ids, Throwable throwable) {
        return ids.stream()
                .map(id -> UserResponseDto.builder()
                        .id(id)
                        .name(UNKNOWN)
                        .surname(UNKNOWN)
                        .email(UNKNOWN)
                        .build())
                .toList();
    }

    private UserResponseDto getUserByEmailFallback(String email, Throwable throwable) {
        throw new ForeignServiceException("UserService is unavailable", throwable);
    }
}

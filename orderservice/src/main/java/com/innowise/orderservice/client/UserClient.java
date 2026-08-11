package com.innowise.orderservice.client;

import com.innowise.orderservice.model.dto.UserResponseDto;
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

    public UserResponseDto getUserById(Long id) {
        return restTemplate.getForObject(
                "/api/users/{id}",
                UserResponseDto.class,
                id
        );
    }

    public List<UserResponseDto> getUsersByIds(List<Long> ids) {
        String idsParam = ids.stream()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
        return Arrays.asList(restTemplate.getForEntity(
                "/api/users/batch/{ids}",
                UserResponseDto[].class,
                idsParam).getBody());
    }

    public UserResponseDto getUserByEmail(String email) {
        return restTemplate.getForObject(
                "/api/users/email/{email}",
                UserResponseDto.class,
                email
        );
    }
}

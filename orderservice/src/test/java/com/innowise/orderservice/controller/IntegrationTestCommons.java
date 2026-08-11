package com.innowise.orderservice.controller;

import com.innowise.orderservice.OrderserviceApplication;
import com.innowise.orderservice.client.UserClient;
import com.innowise.orderservice.model.dto.UserResponseDto;
import com.innowise.orderservice.repository.ItemRepository;
import com.innowise.orderservice.repository.OrderRepository;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.cache.CacheManager;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;

@SpringBootTest(classes = {
        OrderserviceApplication.class,
        IntegrationTestCommons.TestCacheConfig.class
},
        properties = "jwt.secret=jwt-secret-for-test-JzdWIiOiI1Iiwicm9sZSI6IlVTRVIiLCJpYXQiOjE3ODU3NTM1MjAsImV4cC")
@AutoConfigureMockMvc
public abstract class IntegrationTestCommons {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ItemRepository itemRepository;

    @MockitoBean
    protected UserClient userClient;

    static final GenericContainer<?> redis;
    static final PostgreSQLContainer<?> postgres;

    static {
        redis = new GenericContainer<>("redis:7")
                .withExposedPorts(6379);
        redis.start();

        postgres = new PostgreSQLContainer<>("postgres:16")
                .withDatabaseName("orderservice")
                .withUsername("postgres")
                .withPassword("postgres");
        postgres.start();
    }

    @DynamicPropertySource
    static void properties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);

        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @AfterEach
    void cleanDatabase() {
        orderRepository.deleteAll();
        itemRepository.deleteAll();
    }

    protected RequestPostProcessor admin() {
        return authentication(
                new UsernamePasswordAuthenticationToken(
                        1L, null, List.of(new SimpleGrantedAuthority("ADMIN"))
                )
        );
    }

    protected RequestPostProcessor user(Long userId) {
        return authentication(
                new UsernamePasswordAuthenticationToken(
                        userId, null, List.of(new SimpleGrantedAuthority("USER"))
                )
        );
    }

    protected UserResponseDto mockUser(Long userId) {
        UserResponseDto userDto = new UserResponseDto(userId,
                "Ivan",
                "Slesarenko",
                LocalDate.of(2000,1, 1),
                true,
                "ivan@mail.com",
                LocalDateTime.now(),
                LocalDateTime.now());
        when(userClient.getUserById(userId)).thenReturn(userDto);
        return userDto;
    }

    protected void mockUsers(List<UserResponseDto> users) {
        when(userClient.getUsersByIds(anyList())).thenReturn(users);
    }

    protected void mockUserByEmail(String email, UserResponseDto userDto) {
        when(userClient.getUserByEmail(email)).thenReturn(userDto);
    }

    @TestConfiguration
    static class TestCacheConfig {

        @Bean
        CacheManager cacheManager() {
            return new ConcurrentMapCacheManager();
        }
    }
}
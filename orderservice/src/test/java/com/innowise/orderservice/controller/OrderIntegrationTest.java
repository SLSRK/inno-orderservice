package com.innowise.orderservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.innowise.orderservice.model.dto.UserResponseDto;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.http.MediaType.APPLICATION_JSON;

class OrderIntegrationTest extends IntegrationTestCommons {

    private static final String NAME = "Widget";
    private static final Long USER_ID = 5L;
    private static final Long OTHER_USER_ID = 6L;
    private static final Long NON_EXISTENT_ID = 999_999_999L;
    private static final String REF = "/api/orders";
    private static final String REF_W_ID ="/api/orders/{id}";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void createOrder_shouldReturnCreatedOrder_whenAdmin() throws Exception {
        Long itemId = createItem(NAME, 500L);
        mockUser(USER_ID);

        String body = """
                {
                  "userId": %d,
                  "orderItems": [
                    { "itemId": %d, "quantity": 2 }
                  ]
                }
                """.formatted(USER_ID, itemId);

        mockMvc.perform(post(REF)
                        .with(admin())
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.userId").value(USER_ID))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void createOrder_shouldReturnCreatedOrder_whenOwnerCreatesForSelf() throws Exception {
        Long itemId = createItem(NAME, 500L);
        mockUser(USER_ID);

        String body = """
                {
                  "userId": %d,
                  "orderItems": [
                    { "itemId": %d, "quantity": 1 }
                  ]
                }
                """.formatted(USER_ID, itemId);

        mockMvc.perform(post(REF)
                        .with(user(USER_ID))
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated());
    }

    @Test
    void createOrder_shouldReturnForbidden_whenCreatingForAnotherUser() throws Exception {
        Long itemId = createItem(NAME, 500L);
        mockUser(OTHER_USER_ID);

        String body = """
                {
                  "userId": %d,
                  "orderItems": [
                    { "itemId": %d, "quantity": 1 }
                  ]
                }
                """.formatted(OTHER_USER_ID, itemId);

        mockMvc.perform(post(REF)
                        .with(user(USER_ID))
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void createOrder_shouldReturnNotFound_whenItemDoesNotExist() throws Exception {
        mockUser(USER_ID);

        String body = """
                {
                  "userId": %d,
                  "orderItems": [
                    { "itemId": %d, "quantity": 1 }
                  ]
                }
                """.formatted(USER_ID, NON_EXISTENT_ID);

        mockMvc.perform(post(REF)
                        .with(admin())
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void getOrderById_shouldReturnOrder_whenAdmin() throws Exception {
        Long orderId = createOrder(USER_ID, createItem(NAME, 500L), 2L);

        mockMvc.perform(get(REF_W_ID, orderId).with(admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(orderId));
    }

    @Test
    void getOrderById_shouldReturnNotFound_whenDoesNotExist() throws Exception {
        mockMvc.perform(get(REF_W_ID, NON_EXISTENT_ID).with(admin()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getOrderById_shouldReturnForbidden_whenNotOwnerAndNotAdmin() throws Exception {
        Long orderId = createOrder(USER_ID, createItem(NAME, 500L), 1L);

        mockMvc.perform(get(REF_W_ID, orderId).with(user(OTHER_USER_ID)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllOrders_shouldReturnForbidden_whenNotAdmin() throws Exception {
        mockMvc.perform(get(REF).with(user(USER_ID)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getAllOrders_shouldReturnPageOfOrders() throws Exception {
        Long itemId = createItem(NAME, 500L);
        createOrder(USER_ID, itemId, 1L);

        UserResponseDto userDto = mockUser(USER_ID);
        mockUsers(List.of(userDto));

        mockMvc.perform(get(REF).with(admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1));
    }

    @Test
    void getOrdersByUserId_shouldReturnOwnOrders() throws Exception {
        Long itemId = createItem(NAME, 500L);
        createOrder(USER_ID, itemId, 1L);
        mockUser(USER_ID);

        mockMvc.perform(get(REF + "/user/{userId}", USER_ID).with(user(USER_ID)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void getOrdersByUserId_shouldReturnForbidden_whenRequestingAnotherUsersOrders() throws Exception {
        mockMvc.perform(get(REF + "/user/{userId}", OTHER_USER_ID).with(user(USER_ID)))
                .andExpect(status().isForbidden());
    }

    @Test
    void getOrdersByUserId_shouldReturnNotFound_whenNoOrders() throws Exception {
        mockUser(USER_ID);

        mockMvc.perform(get(REF + "/user/{userId}", USER_ID).with(admin()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getOrdersByUserEmail_shouldReturnForbidden_whenNotAdmin() throws Exception {
        mockMvc.perform(get(REF + "/email/{email}", "ivan@test.com").with(user(USER_ID)))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateOrderById_shouldUpdateStatusAndItems() throws Exception {
        Long itemId = createItem(NAME, 500L);
        Long orderId = createOrder(USER_ID, itemId, 1L);
        mockUser(USER_ID);

        String body = """
                {
                  "userId": %d,
                  "status": "PAID",
                  "orderItems": [
                    { "itemId": %d, "quantity": 3 }
                  ]
                }
                """.formatted(USER_ID, itemId);

        mockMvc.perform(put(REF_W_ID, orderId)
                        .with(admin())
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("PAID"));
    }

    @Test
    void updateOrderById_shouldReturnForbidden_whenNotAdmin() throws Exception {
        Long itemId = createItem(NAME, 500L);
        Long orderId = createOrder(USER_ID, itemId, 1L);
        mockUser(USER_ID);

        String body = """
                {
                  "userId": %d,
                  "status": "PAID",
                  "orderItems": [
                    { "itemId": %d, "quantity": 1 }
                  ]
                }
                """.formatted(USER_ID, itemId);

        mockMvc.perform(put(REF_W_ID, orderId)
                        .with(user(USER_ID))
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void updateOrderById_shouldReturnNotFound_whenOrderDoesNotExist() throws Exception {
        Long itemId = createItem(NAME, 500L);
        mockUser(USER_ID);

        String body = """
                {
                  "userId": %d,
                  "status": "PAID",
                  "orderItems": [
                    { "itemId": %d, "quantity": 1 }
                  ]
                }
                """.formatted(USER_ID, itemId);

        mockMvc.perform(put(REF_W_ID, NON_EXISTENT_ID)
                        .with(admin())
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteOrderById_shouldMarkOrderDeleted() throws Exception {
        Long itemId = createItem(NAME, 500L);
        Long orderId = createOrder(USER_ID, itemId, 1L);
        mockUser(USER_ID);

        mockMvc.perform(delete(REF_W_ID, orderId).with(admin()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(REF_W_ID, orderId).with(admin()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteOrderById_shouldReturnForbidden_whenNotOwnerAndNotAdmin() throws Exception {
        Long itemId = createItem(NAME, 500L);
        Long orderId = createOrder(USER_ID, itemId, 1L);

        mockMvc.perform(delete(REF_W_ID, orderId).with(user(OTHER_USER_ID)))
                .andExpect(status().isForbidden());
    }

    @Test
    void deleteOrderById_shouldReturnNotFound_whenDoesNotExist() throws Exception {
        mockMvc.perform(delete(REF_W_ID, NON_EXISTENT_ID).with(admin()))
                .andExpect(status().isNotFound());
    }

    private Long createItem(String name, Long priceInCents) throws Exception {
        String body = """
                {
                  "name": "%s",
                  "priceInCents": %d
                }
                """.formatted(name, priceInCents);

        String response = mockMvc.perform(post("/api/items")
                        .with(admin())
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("id").asLong();
    }

    private Long createOrder(Long userId, Long itemId, Long quantity) throws Exception {
        mockUser(userId);

        String body = """
                {
                  "userId": %d,
                  "orderItems": [
                    { "itemId": %d, "quantity": %d }
                  ]
                }
                """.formatted(userId, itemId, quantity);

        String response = mockMvc.perform(post(REF)
                        .with(admin())
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("id").asLong();
    }
}
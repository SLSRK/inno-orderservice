package com.innowise.orderservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.springframework.http.MediaType.APPLICATION_JSON;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ItemIntegrationTest extends IntegrationTestCommons {

    private static final String NAME = "Widget";
    private static final String NEW_NAME = "Gadget";
    private static final Long PRICE_IN_CENTS = 1_999L;
    private static final Long NEW_PRICE_IN_CENTS = 2_999L;
    private static final Long NON_EXISTENT_ID = 999_999_999L;
    private static final String URI = "/api/v1/items";
    private static final String URI_W_ID = "/api/v1/items/{id}";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void createItem_shouldReturnCreatedItem() throws Exception {
        String body = """
                {
                  "name": "%s",
                  "priceInCents": %d
                }
                """.formatted(NAME, PRICE_IN_CENTS);

        mockMvc.perform(post(URI)
                        .with(admin())
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value(NAME))
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    void createItem_shouldReturnBadRequest_whenNameIsBlank() throws Exception {
        String body = """
                {
                  "name": "",
                  "priceInCents": %d
                }
                """.formatted(PRICE_IN_CENTS);

        mockMvc.perform(post(URI)
                        .with(admin())
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.name").exists());
    }

    @Test
    void createItem_shouldReturnBadRequest_whenPriceIsNegative() throws Exception {
        String body = """
                {
                  "name": "%s",
                  "priceInCents": -100
                }
                """.formatted(NAME);

        mockMvc.perform(post(URI)
                        .with(admin())
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.priceInCents").exists());
    }

    @Test
    void createItem_shouldReturnForbidden_whenNotAdmin() throws Exception {
        String body = """
                {
                  "name": "%s",
                  "priceInCents": %d
                }
                """.formatted(NAME, PRICE_IN_CENTS);

        mockMvc.perform(post(URI)
                        .with(user(2L))
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isForbidden());
    }

    @Test
    void getItemById_shouldReturnItem_whenExists() throws Exception {
        Long itemId = createItem(NAME, PRICE_IN_CENTS);

        mockMvc.perform(get(URI_W_ID, itemId).with(admin()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(itemId))
                .andExpect(jsonPath("$.name").value(NAME));
    }

    @Test
    void getItemById_shouldReturnNotFound_whenDoesNotExist() throws Exception {
        mockMvc.perform(get(URI_W_ID, NON_EXISTENT_ID).with(admin()))
                .andExpect(status().isNotFound());
    }

    @Test
    void getItemById_shouldBeAccessibleByRegularUser() throws Exception {
        Long itemId = createItem(NAME, PRICE_IN_CENTS);

        mockMvc.perform(get(URI_W_ID, itemId).with(user(2L)))
                .andExpect(status().isOk());
    }

    @Test
    void updateItem_shouldUpdateFields() throws Exception {
        Long itemId = createItem(NAME, PRICE_IN_CENTS);

        String body = """
                {
                  "name": "%s",
                  "priceInCents": %d
                }
                """.formatted(NEW_NAME, NEW_PRICE_IN_CENTS);

        mockMvc.perform(put(URI_W_ID, itemId)
                        .with(admin())
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(NEW_NAME));
    }

    @Test
    void updateItem_shouldReturnNotFound_whenDoesNotExist() throws Exception {
        String body = """
                {
                  "name": "%s",
                  "priceInCents": %d
                }
                """.formatted(NEW_NAME, NEW_PRICE_IN_CENTS);

        mockMvc.perform(put(URI_W_ID, NON_EXISTENT_ID)
                        .with(admin())
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteItem_shouldMarkItemDeleted() throws Exception {
        Long itemId = createItem(NAME, PRICE_IN_CENTS);

        mockMvc.perform(delete(URI_W_ID, itemId).with(admin()))
                .andExpect(status().isNoContent());

        mockMvc.perform(get(URI_W_ID, itemId).with(admin()))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteItem_shouldReturnNotFound_whenDoesNotExist() throws Exception {
        mockMvc.perform(delete(URI_W_ID, NON_EXISTENT_ID).with(admin()))
                .andExpect(status().isNotFound());
    }

    private Long createItem(String name, Long priceInCents) throws Exception {
        String body = """
            {
              "name": "%s",
              "priceInCents": %d
            }
            """.formatted(name, priceInCents);

        String response = mockMvc.perform(post(URI)
                        .with(admin())
                        .contentType(APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isCreated())
                .andReturn()
                .getResponse()
                .getContentAsString();

        return objectMapper.readTree(response).get("id").asLong();
    }
}
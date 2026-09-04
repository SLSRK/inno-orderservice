package com.innowise.orderservice.service;

import com.innowise.orderservice.model.dto.OrderCreateDto;
import com.innowise.orderservice.model.dto.OrderResponseDto;
import com.innowise.orderservice.model.dto.OrderUpdateDto;
import com.innowise.orderservice.model.entity.OrderStatus;
import org.springframework.data.domain.Page;

import java.time.LocalDateTime;
import java.util.List;

public interface OrderService {
    /**
     * Create a new order;
     *
     * @param orderCreateDto a data of a new order;
     * @return returns full data of the created order.
     */
    OrderResponseDto createOrder(OrderCreateDto orderCreateDto);

    /**
     * Get an order by id of it exists;
     *
     * @param id the id of an order to get;
     * @return returns full data of the requested order.
     */
    OrderResponseDto getOrderById(Long id);

    /**
     * Retrieves a paginated list of orders with optional filtering;
     *
     * @param status optional status to filter results;
     * @param from optional minimum order date to filter results;
     * @param to optional maximum order date to filter results;
     * @param page the number of page with records;
     * @param size the number of records per page (must be > 0);
     * @return the orders, that match the given criteria.
     */
    Page<OrderResponseDto> getAllOrders(
            List<OrderStatus> status,
            LocalDateTime from,
            LocalDateTime to,
            int page,
            int size
    );

    /**
     * Get orders by customer's account id;
     *
     * @param userId customer's account id;
     * @return returns the list of customer's orders.
     */
    List<OrderResponseDto> getOrdersByUserId(Long userId);

    /**
     * Get orders by customer's account email;
     *
     * @param email customer's account email;
     * @return returns the list of customer's orders.
     */
    List<OrderResponseDto> getOrdersByUserEmail(String email);

    /**
     * Update order's customer, items, quantity, or status by id;
     *
     * @param id the id of an order to update;
     * @param orderUpdateDTO a new data for the order;
     * @return returns full updated data of the order.
     */
    OrderResponseDto updateOrderById(Long id, OrderUpdateDto orderUpdateDTO);

    /**
     * Soft delete order by id;
     *
     * @param id an id of an order to delete;
     * @return returns full data of the deleted order.
     */
    OrderResponseDto deleteOrderById(Long id);

    /**
     * Update order status by ID;
     *
     * @param orderId ID of the order to be updated;
     * @param status new order status;
     */
    void setStatus(Long orderId, OrderStatus status);
}

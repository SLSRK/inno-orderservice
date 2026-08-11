package com.innowise.orderservice.service;

import com.innowise.orderservice.client.UserClient;
import com.innowise.orderservice.exception.NotFoundException;
import com.innowise.orderservice.mapper.OrderMapper;
import com.innowise.orderservice.model.dto.OrderCreateDto;
import com.innowise.orderservice.model.dto.OrderItemRequestDto;
import com.innowise.orderservice.model.dto.OrderResponseDto;
import com.innowise.orderservice.model.dto.OrderUpdateDto;
import com.innowise.orderservice.model.dto.UserResponseDto;
import com.innowise.orderservice.model.entity.Item;
import com.innowise.orderservice.model.entity.Order;
import com.innowise.orderservice.model.entity.OrderStatus;
import com.innowise.orderservice.repository.ItemRepository;
import com.innowise.orderservice.repository.OrderRepository;
import com.innowise.orderservice.service.impl.OrderServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrderUnitTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ItemRepository itemRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private UserClient userClient;

    @InjectMocks
    private OrderServiceImpl orderService;

    @Test
    void createOrder_shouldCreateOrder() {
        OrderCreateDto orderCreateDto = Mockito.mock(OrderCreateDto.class);
        OrderItemRequestDto orderItemRequestDto = new OrderItemRequestDto(1L, 2L);

        Order order = new Order();
        order.setOrderItems(new ArrayList<>());
        Item item = new Item();
        item.setId(1L);
        item.setPrice(500L);

        UserResponseDto userResponseDto = Mockito.mock(UserResponseDto.class);
        OrderResponseDto orderResponseDto = Mockito.mock(OrderResponseDto.class);

        when(orderCreateDto.userId()).thenReturn(1L);
        when(orderCreateDto.orderItems()).thenReturn(List.of(orderItemRequestDto));
        when(userClient.getUserById(1L)).thenReturn(userResponseDto);
        when(orderMapper.toEntity(orderCreateDto)).thenReturn(order);
        when(itemRepository.findByIdInAndDeletedFalse(List.of(1L))).thenReturn(List.of(item));
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toDto(order)).thenReturn(orderResponseDto);

        OrderResponseDto result = orderService.createOrder(orderCreateDto);

        assertEquals(orderResponseDto.id(), result.id());
        assertEquals(orderResponseDto.userId(), result.userId());
        assertEquals(orderResponseDto.status(), result.status());
        assertEquals(orderResponseDto.totalPrice(), result.totalPrice());
        assertEquals(orderResponseDto.deleted(), result.deleted());
        assertEquals(orderResponseDto.createdAt(), result.createdAt());
        assertEquals(orderResponseDto.updatedAt(), result.updatedAt());
        assertEquals(orderResponseDto.orderItems(), result.orderItems());
        assertEquals(userResponseDto, result.user());

        assertEquals(1000L, order.getTotalPrice());
        assertEquals(1, order.getOrderItems().size());
        assertEquals(item, order.getOrderItems().getFirst().getItem());
        assertEquals(2L, order.getOrderItems().getFirst().getQuantity());

        verify(orderRepository).save(order);

    }

    @Test
    void createOrder_shouldThrowWhenItemNotFound() {
        OrderCreateDto orderCreateDto = Mockito.mock(OrderCreateDto.class);

        when(orderCreateDto.userId()).thenReturn(1L);
        when(orderCreateDto.orderItems()).thenReturn(List.of(
                new OrderItemRequestDto(99L, 2L)
        ));
        when(userClient.getUserById(1L))
                .thenReturn(Mockito.mock(UserResponseDto.class));
        when(orderMapper.toEntity(orderCreateDto)).thenReturn(new Order());
        when(itemRepository.findByIdInAndDeletedFalse(List.of(99L)))
                .thenReturn(List.of());

        assertThrows(
                NotFoundException.class,
                () -> orderService.createOrder(orderCreateDto)
        );
    }

    @Test
    void getOrderById_shouldReturnOrder() {
        Order order = testOrder();
        UserResponseDto userResponseDto = Mockito.mock(UserResponseDto.class);
        OrderResponseDto orderResponseDto = Mockito.mock(OrderResponseDto.class);

        when(orderResponseDto.id()).thenReturn(1L);
        when(orderResponseDto.userId()).thenReturn(1L);
        when(orderResponseDto.status()).thenReturn("PENDING");
        when(orderResponseDto.totalPrice()).thenReturn("10.00");
        when(orderResponseDto.deleted()).thenReturn(false);
        when(orderResponseDto.orderItems()).thenReturn(List.of());

        when(orderRepository.findByIdAndDeletedFalse(1L))
                .thenReturn(Optional.of(order));
        when(userClient.getUserById(1L))
                .thenReturn(userResponseDto);
        when(orderMapper.toDto(order))
                .thenReturn(orderResponseDto);

        OrderResponseDto result = orderService.getOrderById(1L);

        assertEquals(1L, result.id());
        assertEquals(1L, result.userId());
        assertEquals("PENDING", result.status());
        assertEquals("10.00", result.totalPrice());
        assertEquals(false, result.deleted());
        assertEquals(List.of(), result.orderItems());
        assertEquals(userResponseDto, result.user());

    }

    @Test
    void getOrderById_shouldThrowWhenNotFound() {
        when(orderRepository.findByIdAndDeletedFalse(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> orderService.getOrderById(1L)
        );
    }

    @Test
    void getAllOrders_shouldReturnOrdersWithUsers() {
        Order order = testOrder();
        UserResponseDto userResponseDto = Mockito.mock(UserResponseDto.class);
        OrderResponseDto orderResponseDto = Mockito.mock(OrderResponseDto.class);

        when(orderResponseDto.userId()).thenReturn(1L);

        Page<Order> page = new PageImpl<>(List.of(order));

        when(orderRepository.findAll(
                any(Specification.class),
                any(Pageable.class)
        )).thenReturn(page);

        when(orderMapper.toDto(order)).thenReturn(orderResponseDto);
        when(userClient.getUsersByIds(List.of(1L)))
                .thenReturn(List.of(userResponseDto));

        Page<OrderResponseDto> result = orderService.getAllOrders(
                List.of(OrderStatus.PENDING),
                null,
                null,
                0,
                10
        );

        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());
    }

    @Test
    void getOrdersByUserId_shouldReturnOrders() {
        Order order = testOrder();
        UserResponseDto userResponseDto = Mockito.mock(UserResponseDto.class);
        OrderResponseDto orderResponseDto = Mockito.mock(OrderResponseDto.class);

        when(userClient.getUserById(1L)).thenReturn(userResponseDto);
        when(userResponseDto.id()).thenReturn(1L);
        when(orderRepository.findByUserIdAndDeletedFalse(1L))
                .thenReturn(List.of(order));
        when(orderMapper.toDto(order)).thenReturn(orderResponseDto);

        List<OrderResponseDto> result = orderService.getOrdersByUserId(1L);

        assertEquals(1, result.size());
    }

    @Test
    void getOrdersByUserId_shouldThrowWhenNoOrders() {
        UserResponseDto userResponseDto = Mockito.mock(UserResponseDto.class);

        when(userClient.getUserById(1L)).thenReturn(userResponseDto);
        when(userResponseDto.id()).thenReturn(1L);
        when(orderRepository.findByUserIdAndDeletedFalse(1L))
                .thenReturn(List.of());

        assertThrows(
                NotFoundException.class,
                () -> orderService.getOrdersByUserId(1L)
        );
    }

    @Test
    void getOrdersByUserEmail_shouldReturnOrders() {
        Order order = testOrder();
        UserResponseDto userResponseDto = Mockito.mock(UserResponseDto.class);
        OrderResponseDto orderResponseDto = Mockito.mock(OrderResponseDto.class);

        when(userClient.getUserByEmail("ivan@mail.com"))
                .thenReturn(userResponseDto);
        when(userResponseDto.id()).thenReturn(1L);
        when(orderRepository.findByUserIdAndDeletedFalse(1L))
                .thenReturn(List.of(order));
        when(orderMapper.toDto(order)).thenReturn(orderResponseDto);

        List<OrderResponseDto> result =
                orderService.getOrdersByUserEmail("ivan@mail.com");

        assertEquals(1, result.size());
    }

    @Test
    void updateOrderById_shouldUpdateOrder() {
        OrderUpdateDto orderUpdateDto = Mockito.mock(OrderUpdateDto.class);
        Order order = testOrder();

        Item item = new Item();
        item.setId(2L);
        item.setPrice(300L);

        UserResponseDto userResponseDto = Mockito.mock(UserResponseDto.class);
        Order updateOrder = new Order();
        OrderResponseDto orderResponseDto = Mockito.mock(OrderResponseDto.class);

        when(orderResponseDto.id()).thenReturn(1L);
        when(orderResponseDto.userId()).thenReturn(2L);
        when(orderResponseDto.status()).thenReturn("PAID");
        when(orderResponseDto.totalPrice()).thenReturn("9.00");
        when(orderResponseDto.deleted()).thenReturn(false);
        when(orderResponseDto.orderItems()).thenReturn(List.of());

        when(orderUpdateDto.userId()).thenReturn(2L);
        when(orderUpdateDto.status()).thenReturn("PAID");
        when(orderUpdateDto.orderItems()).thenReturn(List.of(
                new OrderItemRequestDto(2L, 3L)
        ));

        when(userClient.getUserById(2L)).thenReturn(userResponseDto);
        when(orderRepository.findByIdAndDeletedFalse(1L))
                .thenReturn(Optional.of(order));
        when(orderMapper.toEntity(orderUpdateDto)).thenReturn(updateOrder);
        when(itemRepository.findByIdInAndDeletedFalse(List.of(2L)))
                .thenReturn(List.of(item));
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toDto(order)).thenReturn(orderResponseDto);

        OrderResponseDto result = orderService.updateOrderById(1L, orderUpdateDto);

        assertEquals(1L, result.id());
        assertEquals(2L, result.userId());
        assertEquals("PAID", result.status());
        assertEquals("9.00", result.totalPrice());
        assertEquals(userResponseDto, result.user());

        verify(orderRepository).save(order);

    }

    @Test
    void updateOrderById_shouldThrowWhenNotFound() {
        OrderUpdateDto orderUpdateDto = Mockito.mock(OrderUpdateDto.class);

        when(orderUpdateDto.userId()).thenReturn(1L);
        when(userClient.getUserById(1L))
                .thenReturn(Mockito.mock(UserResponseDto.class));
        when(orderRepository.findByIdAndDeletedFalse(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> orderService.updateOrderById(1L, orderUpdateDto)
        );
    }

    @Test
    void deleteOrderById_shouldMarkOrderDeleted() {
        Order order = testOrder();
        UserResponseDto userResponseDto = Mockito.mock(UserResponseDto.class);
        OrderResponseDto orderResponseDto = Mockito.mock(OrderResponseDto.class);

        when(orderResponseDto.id()).thenReturn(1L);
        when(orderResponseDto.userId()).thenReturn(1L);
        when(orderResponseDto.status()).thenReturn("PENDING");
        when(orderResponseDto.totalPrice()).thenReturn("0.00");
        when(orderResponseDto.deleted()).thenReturn(true);
        when(orderResponseDto.orderItems()).thenReturn(List.of());

        when(orderRepository.findByIdAndDeletedFalse(1L))
                .thenReturn(Optional.of(order));
        when(userClient.getUserById(1L))
                .thenReturn(userResponseDto);
        when(orderRepository.save(order))
                .thenReturn(order);
        when(orderMapper.toDto(order))
                .thenReturn(orderResponseDto);

        OrderResponseDto result = orderService.deleteOrderById(1L);

        assertEquals(1L, result.id());
        assertEquals(1L, result.userId());
        assertEquals("PENDING", result.status());
        assertEquals("0.00", result.totalPrice());
        assertEquals(true, result.deleted());
        assertEquals(userResponseDto, result.user());

        assertEquals(true, order.getDeleted());

        verify(orderRepository).save(order);

    }

    @Test
    void deleteOrderById_shouldThrowWhenNotFound() {
        when(orderRepository.findByIdAndDeletedFalse(1L))
                .thenReturn(Optional.empty());

        assertThrows(
                NotFoundException.class,
                () -> orderService.deleteOrderById(1L)
        );
    }

    private Order testOrder() {
        Order order = new Order();
        order.setId(1L);
        order.setUserId(1L);
        order.setStatus(OrderStatus.PENDING);
        order.setDeleted(false);
        order.setOrderItems(new ArrayList<>());
        return order;
    }
}

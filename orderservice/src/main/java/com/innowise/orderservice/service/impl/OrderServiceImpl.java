package com.innowise.orderservice.service.impl;

import com.innowise.orderservice.client.UserClient;
import com.innowise.orderservice.exception.NotFoundException;
import com.innowise.orderservice.exception.PaymentException;
import com.innowise.orderservice.mapper.OrderMapper;
import com.innowise.orderservice.model.dto.OrderCreateDto;
import com.innowise.orderservice.model.dto.OrderItemRequestDto;
import com.innowise.orderservice.model.dto.OrderResponseDto;
import com.innowise.orderservice.model.dto.OrderUpdateDto;
import com.innowise.orderservice.model.dto.PaymentStatusDto;
import com.innowise.orderservice.model.dto.UserResponseDto;
import com.innowise.orderservice.model.entity.Item;
import com.innowise.orderservice.model.entity.Order;
import com.innowise.orderservice.model.entity.OrderItem;
import com.innowise.orderservice.model.entity.OrderStatus;
import com.innowise.orderservice.repository.ItemRepository;
import com.innowise.orderservice.repository.OrderRepository;
import com.innowise.orderservice.service.OrderService;
import com.innowise.orderservice.specification.OrderSpecification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;
    private final OrderMapper orderMapper;
    private final UserClient userClient;

    @Transactional
    @CacheEvict(value = "orders", allEntries = true)
    public OrderResponseDto createOrder(OrderCreateDto orderCreateDto) {
        log.debug("Creating a new order with the user id={} with {} items",
                orderCreateDto.userId(),
                orderCreateDto.orderItems().size());
        UserResponseDto userResponseDto = userClient.getUserById(orderCreateDto.userId());

        Order order = orderMapper.toEntity(orderCreateDto);
        order.setOrderItems(mapOrderItems(order,orderCreateDto.orderItems()));
        order.setTotalPrice(mapTotalPrice(order.getOrderItems()));
        OrderResponseDto orderResponseDto = orderMapper.toDto(orderRepository.save(order));
        return mapResponseWithUser(orderResponseDto, userResponseDto);
    }

    @Cacheable(value = "orders", key = "#id")
    public OrderResponseDto getOrderById(Long id) {
        Order order = orderRepository.findByIdAndDeletedFalse(id)
                        .orElseThrow(() -> new NotFoundException("Order not found"));

        UserResponseDto userResponseDto = userClient.getUserById(order.getUserId());
        return mapResponseWithUser(orderMapper.toDto(order), userResponseDto);
    }

    public Page<OrderResponseDto> getAllOrders(
            List<OrderStatus> status,
            LocalDateTime from,
            LocalDateTime to,
            int page,
            int size
    ) {

        Pageable pageable = PageRequest.of(page, size);

        Specification<Order> spec = Specification
                .where(OrderSpecification.notDeleted())
                .and(OrderSpecification.hasStatuses(status))
                .and(OrderSpecification.createdAfter(from))
                .and(OrderSpecification.createdBefore(to));

        Page<Order> orderPage = orderRepository.findAll(spec, pageable);

        List<OrderResponseDto> orders = orderPage.getContent().stream()
                .map(orderMapper::toDto)
                .collect(Collectors.toCollection(ArrayList::new));

        List<UserResponseDto> users = userClient.getUsersByIds(orders.stream()
                .map(order -> order.userId())
                .distinct()
                .toList());

        Map<Long, UserResponseDto> userMap = users.stream()
                .collect(Collectors.toMap(
                        UserResponseDto::id,
                        Function.identity(),
                        (existing, duplicate) -> existing
                ));

        List<OrderResponseDto> ordersWithUsers = orders.stream()
                .map(order -> mapResponseWithUser(order, userMap.get(order.userId())))
                .toList();

        return new PageImpl<>(
                ordersWithUsers,
                pageable,
                orderPage.getTotalElements()
        );
    }

    @Cacheable(value = "orders", key = "'user:' + #userId")
    public List<OrderResponseDto> getOrdersByUserId(Long userId) {
        UserResponseDto userResponseDto = userClient.getUserById(userId);
        return getOrdersByUser(userResponseDto);
    }

    @Cacheable(value = "orders", key = "'email:' + #email")
    public List<OrderResponseDto> getOrdersByUserEmail(String email) {
        UserResponseDto userResponseDto = userClient.getUserByEmail(email);
        return getOrdersByUser(userResponseDto);
    }

    @Transactional
    @CacheEvict(value = "orders", allEntries = true)
    public OrderResponseDto updateOrderById(Long id, OrderUpdateDto orderUpdateDto) {
        log.debug("Updating the order with the id={}, with user id={}, status={} and {} items",
                id,
                orderUpdateDto.userId(),
                orderUpdateDto.status(),
                orderUpdateDto.orderItems().size());
        UserResponseDto userResponseDto = userClient.getUserById(orderUpdateDto.userId());

        Order order = orderRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Order not found"));

        Order updateOrder = orderMapper.toEntity(orderUpdateDto);
        order.setUserId(updateOrder.getUserId());
        order.setStatus(updateOrder.getStatus());

        List<OrderItem> orderItems = mapOrderItems(order, orderUpdateDto.orderItems());
        order.getOrderItems().clear();
        order.getOrderItems().addAll(orderItems);
        order.setTotalPrice(mapTotalPrice(order.getOrderItems()));

        OrderResponseDto orderResponseDto = orderMapper.toDto(orderRepository.save(order));
        return mapResponseWithUser(orderResponseDto, userResponseDto);
    }

    @Transactional
    @CacheEvict(value = "orders", allEntries = true)
    public OrderResponseDto deleteOrderById(Long id) {
        log.debug("Deleting the order with the ud={}", id);
        Order order = orderRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new NotFoundException("Order not found"));
        UserResponseDto userResponseDto = userClient.getUserById(order.getUserId());

        order.setDeleted(true);
        OrderResponseDto orderResponseDto = orderMapper.toDto(orderRepository.save(order));
        return mapResponseWithUser(orderResponseDto, userResponseDto);
    }

    @Transactional
    @CacheEvict(value = "orders", allEntries = true)
    public void setStatus(PaymentStatusDto message){
        log.debug("Setting order with the id={} and amount with status={}",
                message.orderId(),
                message.status());
        Order order = orderRepository.findByIdAndDeletedFalse(message.orderId())
                .orElseThrow(() -> new NotFoundException("Order not found"));
        if(message.amount() >= order.getTotalPrice()) {
            order.setStatus(OrderStatus.PAID);
        }
        else {
            throw new PaymentException("Payment amount is not enough");
        }
        orderRepository.save(order);
    }

    private List<OrderItem> mapOrderItems(Order order, List<OrderItemRequestDto> dtos) {
        List<Long> itemIds = dtos.stream().map(OrderItemRequestDto::itemId).toList();
        List<Item> items = itemRepository.findByIdInAndDeletedFalse(itemIds);
        Map<Long, Item> itemsById = items.stream()
                .collect(Collectors.toMap(Item::getId, Function.identity()));

        List<Long> missingIds = itemIds.stream()
                .filter(id -> !itemsById.containsKey(id))
                .toList();
        if (!missingIds.isEmpty()) {
            throw new NotFoundException("Items not found: " + missingIds);
        }

        return dtos.stream()
                .map(dto -> {
                    OrderItem orderItem = new OrderItem();
                    orderItem.setOrder(order);
                    orderItem.setItem(itemsById.get(dto.itemId()));
                    orderItem.setQuantity(dto.quantity());
                    return orderItem;
                })
                .collect(Collectors.toCollection(ArrayList::new));
    }

    private Long mapTotalPrice(List<OrderItem> orderItems){
        return orderItems.stream()
                .mapToLong(item -> item.getItem().getPrice() * item.getQuantity())
                .sum();
    }

    private OrderResponseDto mapResponseWithUser(OrderResponseDto orderResponseDto,
                                                 UserResponseDto userResponseDto) {
        return OrderResponseDto.builder()
                .id(orderResponseDto.id())
                .userId(orderResponseDto.userId())
                .status(orderResponseDto.status())
                .totalPrice(orderResponseDto.totalPrice())
                .deleted(orderResponseDto.deleted())
                .createdAt(orderResponseDto.createdAt())
                .updatedAt(orderResponseDto.updatedAt())
                .orderItems(orderResponseDto.orderItems())
                .user(userResponseDto)
                .build();
    }

    private List<OrderResponseDto> getOrdersByUser(UserResponseDto userResponseDto) {
        List <OrderResponseDto> ordersDto = orderRepository.findByUserIdAndDeletedFalse(userResponseDto.id())
                .stream()
                .map(orderMapper::toDto)
                .collect(Collectors.toCollection(ArrayList::new));
        if(ordersDto.isEmpty()){
            throw new NotFoundException("No orders found");
        }
        OrderResponseDto last = mapResponseWithUser(ordersDto.getLast(), userResponseDto);
        ordersDto.removeLast();
        ordersDto.addLast(last);
        return ordersDto;
    }
}

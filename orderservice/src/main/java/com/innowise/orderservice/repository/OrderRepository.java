package com.innowise.orderservice.repository;

import com.innowise.orderservice.model.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long>, JpaSpecificationExecutor<Order> {

    Optional<Order> findByIdAndDeletedFalse(Long id);

    List<Order> findByUserIdAndDeletedFalse(Long userId);
}

package com.innowise.orderservice.repository;

import com.innowise.orderservice.model.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    Optional<Item> findByIdAndDeletedFalse(Long id);

    List<Item> findByIdInAndDeletedFalse(List<Long> ids);
}

package com.example.team3Project.domain.order.dao;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByAutoOrderStatus(String status);
    List<Order> findByUserId(Long userId);
}

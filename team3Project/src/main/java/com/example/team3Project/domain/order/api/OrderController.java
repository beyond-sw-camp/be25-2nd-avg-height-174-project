package com.example.team3Project.domain.order.api;


import com.example.team3Project.domain.order.application.OrderService;
import com.example.team3Project.domain.order.dao.Order;
import com.example.team3Project.domain.order.dao.OrderRepository;
import com.example.team3Project.domain.order.dto.OrderManagementResponse;
import com.example.team3Project.domain.order.dto.OrderRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderController {
    private final OrderService orderService;
    private final OrderRepository orderRepository;

    @PostMapping
    public ResponseEntity<?> createOrder(@RequestBody OrderRequest request) {

        Order order = orderService.createOrder(request);

        return ResponseEntity.ok(order);
    }

    // 주문 전체 조회
    @GetMapping
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    // 단건 조회
    @GetMapping("/{id}")
    public Order getOrder(@PathVariable Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("주문 없음"));
    }

    // 주문 취소
    @PatchMapping("/{id}/cancel")
    public Order cancelOrder(@PathVariable Long id) {

        return orderService.cancelOrder(id);
    }


    // 주문 관리
    @GetMapping("/management")
    public List<OrderManagementResponse> getOrderManagement() {

        return orderService.getOrderManagement();
    }

    // 주문 실패 내역 조회
    @GetMapping("/failed")
    public List<Order> getFailedOrders() {

        return orderService.getFailedOrders();
    }

    // 사용자 주문 조회
    @GetMapping("/user/{userId}")
    public List<Order> getOrdersByUser(@PathVariable Long userId) {

        return orderService.getOrdersByUser(userId);
    }
}

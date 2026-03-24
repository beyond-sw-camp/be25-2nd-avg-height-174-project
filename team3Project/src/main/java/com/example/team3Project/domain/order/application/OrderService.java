package com.example.team3Project.domain.order.application;


import com.example.team3Project.domain.order.dao.Order;
import com.example.team3Project.domain.order.dao.OrderItem;
import com.example.team3Project.domain.order.dao.OrderRepository;
import com.example.team3Project.domain.order.dto.OrderItemRequest;
import com.example.team3Project.domain.order.dto.OrderRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;

    // 주문 생성
    @Transactional
    public Order createOrder(OrderRequest request) {

        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setStatus("CREATED");

        List<OrderItem> items = new ArrayList<>();
        int totalAmount = 0;

        if (request.getItems() == null) {
            throw new RuntimeException("No items");
        }

        for (OrderItemRequest itemReq : request.getItems()) {
            OrderItem item = new OrderItem();
            item.setProductId(itemReq.getProductId());
            item.setProductName(itemReq.getProductName());
            item.setPrice(itemReq.getPrice());
            item.setQuantity(itemReq.getQuantity());
            item.setOrder(order);

            totalAmount += itemReq.getPrice() * itemReq.getQuantity();
            items.add(item);
        }

        order.setItems(items);
        order.setTotalAmount(totalAmount);

        return orderRepository.save(order);
    }

    // 주문 취소
    @Transactional
    public Order cancelOrder(Long orderId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("주문 없음"));

        if ("PAID".equals(order.getStatus())) {
            throw new RuntimeException("결제된 주문은 취소 불가");
        }

        order.setStatus("CANCELLED");

        return orderRepository.save(order);
    }


}

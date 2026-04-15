package com.example.team3Project.domain.order.application;


import com.example.team3Project.domain.order.dao.Order;
import com.example.team3Project.domain.order.dao.OrderItem;
import com.example.team3Project.domain.order.dao.OrderRepository;
import com.example.team3Project.domain.order.dto.OrderItemRequest;
import com.example.team3Project.domain.order.dto.OrderManagementResponse;
import com.example.team3Project.domain.order.dto.OrderRequest;
import com.example.team3Project.domain.settlement.application.PaymentService;
import com.example.team3Project.domain.settlement.dao.Payment;
import com.example.team3Project.domain.settlement.enums.PaymentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final PaymentService paymentService;

    // 주문 생성
    @Transactional
    public Order createOrder(OrderRequest request) {

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new RuntimeException("주문 상품이 없습니다.");
        }

        if (request.getCardId() == null) {
            throw new RuntimeException("cardId가 필요합니다.");
        }


        Order order = new Order();

        order.setUserId(request.getUserId());
        order.setCustomerName(request.getCustomerName());
        order.setCustomerPhone(request.getCustomerPhone());

        order.setStatus("CREATED");
        order.setAutoOrderStatus("Ready");

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

        Order savedOrder = orderRepository.save(order);

        // 주문 생성 후 결제 처리
        paymentService.processPayment(savedOrder.getId(), request.getCardId());


        return savedOrder;
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

    @Transactional(readOnly = true)
    public List<OrderManagementResponse> getOrderManagement() {

        List<Order> orders = orderRepository.findAll();

        return orders.stream().map(order -> {

            OrderManagementResponse res = new OrderManagementResponse();

            res.setOrderId(order.getId());
            res.setAutoOrderStatus(order.getAutoOrderStatus());

            res.setCustomerName(order.getCustomerName());
            res.setCustomerPhone(order.getCustomerPhone());

            // 모든 상품 나열
            List<OrderItem> items = order.getItems();

            if (items != null && !items.isEmpty()) {

                String productNames = items.stream()
                        .map(OrderItem::getProductName)
                        .collect(Collectors.joining(", "));

                res.setProductName(productNames);
            }

            res.setOverseasMall(order.getOverseasMall());
            res.setPaymentAmount(order.getTotalAmount());
            res.setMargin(order.getMargin());

            return res;

        }).toList();
    }

    // 실패 내역
    @Transactional(readOnly = true)
    public List<Order> getFailedOrders() {

        return orderRepository.findByAutoOrderStatus("FAILED");
    }

    // 사용자 조회
    @Transactional(readOnly = true)
    public List<Order> getOrdersByUser(Long userId) {

        return orderRepository.findByUserId(userId);
    }
}

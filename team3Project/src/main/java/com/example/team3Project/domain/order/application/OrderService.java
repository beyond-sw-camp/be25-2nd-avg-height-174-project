package com.example.team3Project.domain.order.application;


import com.example.team3Project.domain.order.dao.Order;
import com.example.team3Project.domain.order.dao.OrderRepository;
import com.example.team3Project.domain.order.dto.MonthlyRevenueResponse;
import com.example.team3Project.domain.order.dto.OrderManagementResponse;
import com.example.team3Project.domain.order.dto.OrderRequest;
import com.example.team3Project.domain.product.dao.DummyCoupangProduct;
import com.example.team3Project.domain.product.dao.DummyCoupangProductRepository;
import com.example.team3Project.domain.settlement.application.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
public class OrderService {
    private final OrderRepository orderRepository;
    private final PaymentService paymentService;
    private final DummyCoupangProductRepository productRepository;

    // 주문 생성
    @Transactional
    public Order createOrder(OrderRequest request) {

        if (request.getDummyCoupangProductId() == null) {
            throw new RuntimeException("주문 상품이 없습니다.");
        }
        if (request.getQuantity() <= 0) {
            throw new RuntimeException("주문 수량은 1 이상이어야 합니다.");
        }

        DummyCoupangProduct product = productRepository.findById(request.getDummyCoupangProductId())
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다: " + request.getDummyCoupangProductId()));

        // dummy_coupang_product의 order_count를 주문 수량만큼 증가
        product.incrementOrderCount(request.getQuantity());

        Order order = new Order();
        order.setUserId(request.getUserId());
        order.setCustomerName(request.getCustomerName());
        order.setCustomerPhone(request.getCustomerPhone());
        order.setCustomerAddress(request.getCustomerAddress());
        order.setCustomsNumber(request.getCustomsNumber());
        order.setDummyCoupangProduct(product);
        order.setProductName(product.getProductName());
        int qty = request.getQuantity();
        order.setQuantity(qty);
        order.setTotalAmount(product.getSalePrice().multiply(BigDecimal.valueOf(qty)).intValue());

        // margin_krw × 수량 → 주문 마진 저장
        int orderMargin = (product.getMarginKrw() != null)
                ? product.getMarginKrw().multiply(BigDecimal.valueOf(qty)).intValue()
                : 0;
        order.setMargin(orderMargin);

        order.setStatus("CREATED");
        order.setAutoOrderStatus("Ready");

        Order savedOrder = orderRepository.save(order);

        // 카드 ID가 있을 때만 결제 처리
        if (request.getCardId() != null) {
            paymentService.processPayment(savedOrder.getId(), request.getCardId());
        }

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
            res.setCustomerAddress(order.getCustomerAddress());
            res.setCustomsNumber(order.getCustomsNumber());
            res.setDummyCoupangProductId(order.getDummyCoupangProductId());
            res.setProductName(order.getProductName());
            res.setQuantity(order.getQuantity());
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

    // 사용자 등록 상품의 월별 수익 집계
    @Transactional(readOnly = true)
    public List<MonthlyRevenueResponse> getMonthlyRevenue(Long userId) {

        return orderRepository.findMonthlyRevenueByUserId(userId).stream()
                .map(row -> {
                    int year       = ((Number) row[0]).intValue();
                    int month      = ((Number) row[1]).intValue();
                    long orderCount = ((Number) row[2]).longValue();
                    long sales     = ((Number) row[3]).longValue();
                    long margin    = ((Number) row[4]).longValue();
                    double profitRate = (sales > 0) ? (margin * 100.0 / sales) : 0.0;

                    return new MonthlyRevenueResponse(year, month, orderCount, sales, margin, profitRate);
                })
                .toList();
    }
}

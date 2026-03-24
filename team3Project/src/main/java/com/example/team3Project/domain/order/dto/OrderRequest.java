package com.example.team3Project.domain.order.dto;

import lombok.Data;

import java.util.List;

@Data
public class OrderRequest {
    private Long userId;
    private String paymentMethod;
    private List<OrderItemRequest> items;
    private Long cardId;
    private String cardNumber;
}

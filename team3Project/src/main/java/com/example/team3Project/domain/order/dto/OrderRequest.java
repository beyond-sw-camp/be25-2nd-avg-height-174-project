package com.example.team3Project.domain.order.dto;

import lombok.Data;

import java.util.List;

@Data
public class OrderRequest {
    private Long userId;

    // 고객 정보
    private String customerName;
    private String customerPhone;

    private List<OrderItemRequest> items;
    private Long cardId;
}

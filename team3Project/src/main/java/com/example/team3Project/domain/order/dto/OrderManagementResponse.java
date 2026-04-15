package com.example.team3Project.domain.order.dto;

import lombok.Data;

@Data
public class OrderManagementResponse {

    private Long orderId;

    private String autoOrderStatus;

    private String customerName;
    private String customerPhone;

    private String productName;
    private int price;

    private String overseasMall;

    private int paymentAmount;

    private int margin;
}

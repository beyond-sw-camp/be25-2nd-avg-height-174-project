package com.example.team3Project.domain.order.dao;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Setter
@Entity
@Table(name = "orders")
public class Order {
    @Id
    @GeneratedValue
    private Long id;

    private Long userId;
    private int totalAmount;
    private String status; // created / paid / canceled

    // 화면설계서
    private String customerName;    // 고객 이름
    private String customerPhone;   // 고객 번호

    private String autoOrderStatus; // 자동 주문 상태 Ready / Ordered/ Failed / Shipping

    private final String overseasMall = "Amazon"; // 해외몰 정보 (지금은 Amazon 1개)

    private int margin; // 마진

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> items;
}

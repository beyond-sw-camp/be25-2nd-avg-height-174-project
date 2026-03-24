package com.example.team3Project.domain.settlement.dao;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Entity
@Table(name = "card")
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String cardType;   // VISA, MASTER, KAKAO
    private String cardNumber;
    private int balance;

    @Column(name = "card_limit")
    private int cardLimit;
    private boolean active; // 활성화/비활성화
}
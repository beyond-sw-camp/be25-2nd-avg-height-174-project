package com.example.team3Project.domain.policy.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity     // JPA가 관리하는 엔티티 클래스 - DB가 관리
@Table(name = "user_policy_setting")  // 이 클래스가 연결될 테이블 이름
@Getter
@NoArgsConstructor
public class UserPolicySetting {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)      // 자동 증가 값으로 생성
    @Column(name = "user_policy_setting_id")                 // DB 컬러명을 명시적으로 지정
    private Long userPolicySettingId;

    @Column(name = "user_id", nullable = false, unique = true)
    private Long userId; // 사용자id
    @Column(name = "target_margin_rate", nullable = false)
    private BigDecimal targetMarginRate;  // 목표 마진율
    @Column(name = "min_margin_amount", nullable = false)
    private BigDecimal minMarginAmount;   // 최소 마진(원)
    @Column(name = "market_fee_rate", nullable = false)
    private BigDecimal marketFeeRate;     // 마켓 수수료율(%)
    @Column(name = "card_fee_rate", nullable = false)
    private BigDecimal cardFeeRate;       // 카드 수수료(%)


    // 새 정책 설정 엔티티를 만들 때 사용하는 정책 생성 메서드
    public static UserPolicySetting create(
            Long userId,
            BigDecimal targetMarginRate,
            BigDecimal minMarginAmount,
            BigDecimal marketFeeRate,
            BigDecimal cardFeeRate
    ) {
        UserPolicySetting setting = new UserPolicySetting();
        setting.userId = userId;
        setting.targetMarginRate = targetMarginRate;
        setting.minMarginAmount = minMarginAmount;
        setting.marketFeeRate = marketFeeRate;
        setting.cardFeeRate = cardFeeRate;
        return setting;
    }

    // 기존 엔티티의 값을 수정
    public void update(
            BigDecimal targetMarginRate,
            BigDecimal minMarginAmount,
            BigDecimal marketFeeRate,
            BigDecimal cardFeeRate
    ) {
        this.targetMarginRate = targetMarginRate;
        this.minMarginAmount = minMarginAmount;
        this.marketFeeRate = marketFeeRate;
        this.cardFeeRate = cardFeeRate;
    }
}




package com.example.team3Project.domain.policy.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

@Getter
@AllArgsConstructor
// 서버가 클라이언트에게 응답으로 돌려줄 데이터를 담는 객체이다.
public class PolicySettingResponse {
    private Long userPolicySettingId;       // user_policy_setting 테이블의 PK
    private Long userId;
    private BigDecimal targetMarginRate;
    private BigDecimal minMarginAmount;
    private BigDecimal marketFeeRate;
    private BigDecimal cardFeeRate;
}

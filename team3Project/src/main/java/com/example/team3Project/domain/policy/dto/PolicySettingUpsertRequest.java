package com.example.team3Project.domain.policy.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

// JSON 데이터를 자바 객체로 받기 위한 파일
// 정책 설정 저장,수정 요청 데이터를 담는 DTO
@Getter
@NoArgsConstructor
public class PolicySettingUpsertRequest {
    @NotNull
    @DecimalMin(value = "0.0")  // 해당 숫자는 0.0이상이어야 한다.
    private BigDecimal targetMarginRate; // 목표 마진율

    @NotNull
    @DecimalMin(value = "0.0")
    private BigDecimal minMarginAmount; // 최소 마진(원)

    @NotNull
    @DecimalMin(value = "0.0")
    private BigDecimal marketFeeRate;   // 마켓 수수료율(%)

    @NotNull
    @DecimalMin(value = "0.0")
    private BigDecimal cardFeeRate;    // 카드 수수료(%)
}

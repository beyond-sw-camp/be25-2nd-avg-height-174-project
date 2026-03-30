package com.example.team3Project.domain.product.processing.application;

import com.example.team3Project.domain.policy.application.PolicyQueryService;
import com.example.team3Project.domain.policy.dto.BlockedWordResponse;
import com.example.team3Project.domain.policy.dto.PolicyBundle;
import com.example.team3Project.domain.policy.dto.PolicySettingResponse;
import com.example.team3Project.domain.policy.dto.ReplacementWordResponse;
import com.example.team3Project.domain.policy.entity.MarketCode;
import com.example.team3Project.domain.policy.entity.PriceRoundingUnit;
import com.example.team3Project.domain.policy.entity.ShippingFeeType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProductProcessingServiceTest {

    @Test
    @DisplayName("금지어가 포함된 상품명은 가공 대상에서 제외된다")
    void processProductName_returnsEmpty_whenBlockedWordExists() {
        PolicyQueryService policyQueryService = mock(PolicyQueryService.class);
        ProductProcessingService productProcessingService = new ProductProcessingService(policyQueryService);

        PolicyBundle policyBundle = new PolicyBundle(
                defaultPolicySetting(),
                List.of(new BlockedWordResponse(1L, 1L, "무료배송")),
                List.of()
        );

        when(policyQueryService.getPolicyBundle(1L, MarketCode.COUPANG)).thenReturn(policyBundle);

        Optional<String> result =
                productProcessingService.processProductName(1L, MarketCode.COUPANG, "무료배송 이동식");

        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("치환어가 존재하면 상품명에 치환 규칙을 적용한다")
    void processProductName_appliesReplacementWords() {
        PolicyQueryService policyQueryService = mock(PolicyQueryService.class);
        ProductProcessingService productProcessingService = new ProductProcessingService(policyQueryService);

        PolicyBundle policyBundle = new PolicyBundle(
                defaultPolicySetting(),
                List.of(),
                List.of(
                        new ReplacementWordResponse(1L, 1L, "당일발송", "빠른출고"),
                        new ReplacementWordResponse(2L, 1L, "무료배송", "배송비포함")
                )
        );

        when(policyQueryService.getPolicyBundle(1L, MarketCode.COUPANG)).thenReturn(policyBundle);

        Optional<String> result =
                productProcessingService.processProductName(1L, MarketCode.COUPANG, "무료배송 당일발송 이동식");

        assertTrue(result.isPresent());
        assertEquals("배송비포함 빠른출고 이동식", result.get());
    }

    @Test
    @DisplayName("금지어나 치환어가 없으면 원본 상품명이 그대로 유지된다")
    void processProductName_returnsOriginalName_whenNoPolicyMatches() {
        PolicyQueryService policyQueryService = mock(PolicyQueryService.class);
        ProductProcessingService productProcessingService = new ProductProcessingService(policyQueryService);

        PolicyBundle policyBundle = new PolicyBundle(
                defaultPolicySetting(),
                List.of(),
                List.of()
        );

        when(policyQueryService.getPolicyBundle(1L, MarketCode.COUPANG)).thenReturn(policyBundle);

        Optional<String> result =
                productProcessingService.processProductName(1L, MarketCode.COUPANG, "기본 이동식");

        assertTrue(result.isPresent());
        assertEquals("기본 이동식", result.get());
    }

    private PolicySettingResponse defaultPolicySetting() {
        return new PolicySettingResponse(
                1L,
                1L,
                MarketCode.COUPANG,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.ZERO,
                BigDecimal.valueOf(1350),
                PriceRoundingUnit.HUNDRED_WON,
                false,
                false,
                true,
                false,
                true,
                ShippingFeeType.PAID_SHIPPING,
                BigDecimal.valueOf(3000),
                BigDecimal.valueOf(5000),
                BigDecimal.valueOf(5000)
        );
    }
}

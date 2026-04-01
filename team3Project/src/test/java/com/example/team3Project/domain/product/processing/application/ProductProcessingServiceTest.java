package com.example.team3Project.domain.product.processing.application;

import com.example.team3Project.domain.policy.application.PolicyQueryService;
import com.example.team3Project.domain.policy.dto.BlockedWordResponse;
import com.example.team3Project.domain.policy.dto.PolicyBundle;
import com.example.team3Project.domain.policy.dto.PolicySettingResponse;
import com.example.team3Project.domain.policy.dto.ReplacementWordResponse;
import com.example.team3Project.domain.policy.entity.MarketCode;
import com.example.team3Project.domain.policy.entity.PriceRoundingUnit;
import com.example.team3Project.domain.policy.entity.ShippingFeeType;
import com.example.team3Project.domain.product.processing.dto.ProductProcessingRequest;
import com.example.team3Project.domain.product.processing.dto.ProductProcessingResultResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProductProcessingServiceTest {

    @Test
    @DisplayName("금지어가 포함된 상품명은 기존 상품명 가공 흐름에서 제외된다")
    void processProductName_returnsEmpty_whenBlockedWordExists() {
        // given
        PolicyQueryService policyQueryService = mock(PolicyQueryService.class);
        ProductProcessingService productProcessingService = new ProductProcessingService(policyQueryService);

        PolicyBundle policyBundle = new PolicyBundle(
                defaultPolicySetting(),
                List.of(new BlockedWordResponse(1L, 1L, "무료배송")),
                List.of()
        );

        when(policyQueryService.getPolicyBundle(1L, MarketCode.COUPANG)).thenReturn(policyBundle);

        // when
        Optional<String> result =
                productProcessingService.processProductName(1L, MarketCode.COUPANG, "무료배송 이동식 선반");

        // then
        assertTrue(result.isEmpty());
    }

    @Test
    @DisplayName("치환어가 있으면 기존 상품명 가공 흐름에서 치환이 적용된다")
    void processProductName_appliesReplacementWords() {
        // given
        PolicyQueryService policyQueryService = mock(PolicyQueryService.class);
        ProductProcessingService productProcessingService = new ProductProcessingService(policyQueryService);

        PolicyBundle policyBundle = new PolicyBundle(
                defaultPolicySetting(),
                List.of(),
                List.of(
                        new ReplacementWordResponse(1L, 1L, "빠른배송", "당일출고"),
                        new ReplacementWordResponse(2L, 1L, "무료배송", "배송비포함")
                )
        );

        when(policyQueryService.getPolicyBundle(1L, MarketCode.COUPANG)).thenReturn(policyBundle);

        // when
        Optional<String> result =
                productProcessingService.processProductName(1L, MarketCode.COUPANG, "무료배송 빠른배송 이동식 선반");

        // then
        assertTrue(result.isPresent());
        assertEquals("배송비포함 당일출고 이동식 선반", result.get());
    }

    @Test
    @DisplayName("금지어와 치환어가 없으면 기존 상품명 가공 흐름은 원본을 그대로 반환한다")
    void processProductName_returnsOriginalName_whenNoPolicyMatches() {
        // given
        PolicyQueryService policyQueryService = mock(PolicyQueryService.class);
        ProductProcessingService productProcessingService = new ProductProcessingService(policyQueryService);

        PolicyBundle policyBundle = new PolicyBundle(
                defaultPolicySetting(),
                List.of(),
                List.of()
        );

        when(policyQueryService.getPolicyBundle(1L, MarketCode.COUPANG)).thenReturn(policyBundle);

        // when
        Optional<String> result =
                productProcessingService.processProductName(1L, MarketCode.COUPANG, "기본 이동식 선반");

        // then
        assertTrue(result.isPresent());
        assertEquals("기본 이동식 선반", result.get());
    }

    @Test
    @DisplayName("금지어가 포함되면 상품 가공 흐름은 BLOCKED 상태로 조기 반환한다")
    void processProduct_returnsBlocked_whenBlockedWordExists() {
        // given
        // processProduct는 translatedProductName을 기준으로 금지어를 검사한다.
        PolicyQueryService policyQueryService = mock(PolicyQueryService.class);
        ProductProcessingService productProcessingService = new ProductProcessingService(policyQueryService);

        PolicyBundle policyBundle = new PolicyBundle(
                defaultPolicySetting(),
                List.of(new BlockedWordResponse(1L, 1L, "금지어")),
                List.of()
        );

        when(policyQueryService.getPolicyBundle(1L, MarketCode.COUPANG)).thenReturn(policyBundle);

        ProductProcessingRequest request = createRequest("금지어 포함 상품", "브랜드", BigDecimal.valueOf(10));

        // when
        ProductProcessingResultResponse result =
                productProcessingService.processProduct(1L, MarketCode.COUPANG, request);

        // then
        assertTrue(result.isExcluded());
        assertEquals("BLOCKED_WORD", result.getExclusionReason());
        assertEquals("BLOCKED", result.getRegistrationStatus());
        assertNull(result.getCostInKrw());
        assertNull(result.getSalePrice());
        assertNull(result.getShippingFee());
    }

    @Test
    @DisplayName("최소 마진 보호가 꺼져 있으면 목표 마진율 기준으로만 판매가를 계산한다")
    void processProduct_usesMarginRateOnly_whenMinMarginProtectDisabled() {
        // given
        PolicyQueryService policyQueryService = mock(PolicyQueryService.class);
        ProductProcessingService productProcessingService = new ProductProcessingService(policyQueryService);

        PolicyBundle policyBundle = new PolicyBundle(
                policySettingWithMinMarginProtect(false, BigDecimal.valueOf(30), BigDecimal.valueOf(5000)),
                List.of(),
                List.of()
        );

        when(policyQueryService.getPolicyBundle(1L, MarketCode.COUPANG)).thenReturn(policyBundle);

        ProductProcessingRequest request = createRequest("정상 상품", "브랜드", BigDecimal.valueOf(10));

        ProductProcessingResultResponse result =
                productProcessingService.processProduct(1L, MarketCode.COUPANG, request);

        // 원가 10달러 * 환율 1350 = 13,500원
        // 목표 마진율 30% => 4,050원
        // 최소 마진 보호가 꺼져 있으므로 5,000원이 아니라 4,050원을 사용한다.
        assertEquals(BigDecimal.valueOf(13500), result.getCostInKrw());
        assertEquals(BigDecimal.valueOf(20300), result.getSalePrice());
    }

    @Test
    @DisplayName("수수료율 합계가 100퍼센트 이상이면 예외가 발생한다")
    void processProduct_throwsException_whenTotalFeeRateIsInvalid() {
        // given
        PolicyQueryService policyQueryService = mock(PolicyQueryService.class);
        ProductProcessingService productProcessingService = new ProductProcessingService(policyQueryService);

        PolicyBundle policyBundle = new PolicyBundle(
                policySettingWithFee(BigDecimal.valueOf(70), BigDecimal.valueOf(30)),
                List.of(),
                List.of()
        );

        when(policyQueryService.getPolicyBundle(1L, MarketCode.COUPANG)).thenReturn(policyBundle);

        ProductProcessingRequest request = createRequest("정상 상품", "브랜드", BigDecimal.valueOf(10));

        assertThrows(IllegalArgumentException.class,
                () -> productProcessingService.processProduct(1L, MarketCode.COUPANG, request));
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

    private PolicySettingResponse policySettingWithMinMarginProtect(
            boolean minMarginProtectEnabled,
            BigDecimal targetMarginRate,
            BigDecimal minMarginAmount
    ) {
        return new PolicySettingResponse(
                1L,
                1L,
                MarketCode.COUPANG,
                targetMarginRate,
                minMarginAmount,
                BigDecimal.valueOf(10),
                BigDecimal.valueOf(3.3),
                BigDecimal.valueOf(1350),
                PriceRoundingUnit.HUNDRED_WON,
                false,
                false,
                minMarginProtectEnabled,
                false,
                true,
                ShippingFeeType.PAID_SHIPPING,
                BigDecimal.valueOf(3000),
                BigDecimal.valueOf(5000),
                BigDecimal.valueOf(5000)
        );
    }

    private PolicySettingResponse policySettingWithFee(BigDecimal marketFeeRate, BigDecimal cardFeeRate) {
        return new PolicySettingResponse(
                1L,
                1L,
                MarketCode.COUPANG,
                BigDecimal.valueOf(30),
                BigDecimal.valueOf(5000),
                marketFeeRate,
                cardFeeRate,
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

    private ProductProcessingRequest createRequest(String translatedProductName, String translatedBrand, BigDecimal originalPrice) {
        ProductProcessingRequest request = new ProductProcessingRequest();
        ReflectionTestUtils.setField(request, "sourceProductId", "ASIN-001");
        ReflectionTestUtils.setField(request, "sourceUrl", "https://www.amazon.com/dp/ASIN-001");
        ReflectionTestUtils.setField(request, "translatedProductName", translatedProductName);
        ReflectionTestUtils.setField(request, "translatedBrand", translatedBrand);
        ReflectionTestUtils.setField(request, "originalPrice", originalPrice);
        ReflectionTestUtils.setField(request, "currency", "USD");
        ReflectionTestUtils.setField(request, "mainImageUrl", "https://image.example/main.jpg");
        ReflectionTestUtils.setField(request, "descriptionImageUrls", List.of("https://image.example/detail-1.jpg"));
        return request;
    }
}

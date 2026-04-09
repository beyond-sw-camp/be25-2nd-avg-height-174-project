package com.example.team3Project.domain.product.coupang.application;

import com.example.team3Project.domain.policy.entity.MarketCode;
import com.example.team3Project.domain.product.coupang.dao.DummyCoupangProductRepository;
import com.example.team3Project.domain.product.coupang.entity.DummyCoupangProduct;
import com.example.team3Project.domain.product.registration.dao.DummyProductRegistrationRepository;
import com.example.team3Project.domain.product.registration.entity.DummyProductImage;
import com.example.team3Project.domain.product.registration.entity.DummyProductImageType;
import com.example.team3Project.domain.product.registration.entity.DummyProductOption;
import com.example.team3Project.domain.product.registration.entity.DummyProductRegistration;
import com.example.team3Project.domain.product.registration.entity.RegistrationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

class DummyCoupangProductServiceTest {

    private DummyCoupangProductRepository coupangProductRepository;
    private DummyProductRegistrationRepository registrationRepository;
    private DummyCoupangProductService service;

    @BeforeEach
    void setUp() {
        coupangProductRepository = mock(DummyCoupangProductRepository.class);
        registrationRepository = mock(DummyProductRegistrationRepository.class);
        service = new DummyCoupangProductService(coupangProductRepository, registrationRepository);
    }

    @Test
    @DisplayName("등록 가능 상태의 쿠팡 등록 후보를 쿠팡 더미 상품으로 발행한다")
    void publish_createsCoupangProduct() {
        DummyProductRegistration registration = createRegistration(10L, RegistrationStatus.READY, MarketCode.COUPANG);
        registration.addOption(DummyProductOption.create("OPT-1", "{\"색상\":\"블랙\"}", true, BigDecimal.TEN, "USD", "In Stock", null, null));
        registration.addImage(DummyProductImage.create(DummyProductImageType.MAIN, null, "main-image", 0));

        when(registrationRepository.findByDummyProductRegistrationIdAndUserId(10L, 1L))
                .thenReturn(Optional.of(registration));
        when(coupangProductRepository.findByUserIdAndSourceProductId(1L, "ASIN-001"))
                .thenReturn(Optional.empty());
        when(coupangProductRepository.save(any(DummyCoupangProduct.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DummyCoupangProduct product = service.publish(1L, 10L);

        assertEquals("가공 상품명", product.getProductName());
        assertEquals(1, product.getOptions().size());
        assertEquals(1, product.getImages().size());
        assertEquals(RegistrationStatus.REGISTERED, registration.getRegistrationStatus());
        verify(coupangProductRepository).save(any(DummyCoupangProduct.class));
    }

    @Test
    @DisplayName("이미 발행된 같은 원본 상품은 기존 쿠팡 더미 상품을 갱신한다")
    void publish_updatesExistingCoupangProduct() {
        DummyProductRegistration registration = createRegistration(10L, RegistrationStatus.READY, MarketCode.COUPANG);
        DummyCoupangProduct existing = DummyCoupangProduct.create(registration);
        ReflectionTestUtils.setField(existing, "dummyCoupangProductId", 77L);

        when(registrationRepository.findByDummyProductRegistrationIdAndUserId(10L, 1L))
                .thenReturn(Optional.of(registration));
        when(coupangProductRepository.findByUserIdAndSourceProductId(1L, "ASIN-001"))
                .thenReturn(Optional.of(existing));
        when(coupangProductRepository.save(existing)).thenReturn(existing);

        DummyCoupangProduct result = service.publish(1L, 10L);

        assertSame(existing, result);
        assertEquals(RegistrationStatus.REGISTERED, registration.getRegistrationStatus());
        verify(coupangProductRepository).save(existing);
    }

    @Test
    @DisplayName("차단된 등록 후보는 쿠팡 더미 마켓으로 발행할 수 없다")
    void publish_throwsBadRequest_whenRegistrationBlocked() {
        DummyProductRegistration registration = createRegistration(10L, RegistrationStatus.BLOCKED, MarketCode.COUPANG);

        when(registrationRepository.findByDummyProductRegistrationIdAndUserId(10L, 1L))
                .thenReturn(Optional.of(registration));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.publish(1L, 10L)
        );

        assertEquals(BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    @DisplayName("쿠팡이 아닌 마켓 등록 후보는 쿠팡 더미 마켓으로 발행할 수 없다")
    void publish_throwsBadRequest_whenRegistrationMarketIsNotCoupang() {
        DummyProductRegistration registration = createRegistration(10L, RegistrationStatus.READY, MarketCode.NAVER_SMART_STORE);

        when(registrationRepository.findByDummyProductRegistrationIdAndUserId(10L, 1L))
                .thenReturn(Optional.of(registration));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.publish(1L, 10L)
        );

        assertEquals(BAD_REQUEST, exception.getStatusCode());
    }

    @Test
    @DisplayName("선택한 등록 후보 여러 건을 쿠팡 더미 상품으로 한 번에 발행한다")
    void publishAll_publishesSelectedRegistrations() {
        DummyProductRegistration first = createRegistration(10L, RegistrationStatus.READY, MarketCode.COUPANG);
        DummyProductRegistration second = createRegistration(11L, RegistrationStatus.READY, MarketCode.COUPANG);
        ReflectionTestUtils.setField(second, "sourceProductId", "ASIN-002");

        when(registrationRepository.findByDummyProductRegistrationIdAndUserId(10L, 1L)).thenReturn(Optional.of(first));
        when(registrationRepository.findByDummyProductRegistrationIdAndUserId(11L, 1L)).thenReturn(Optional.of(second));
        when(coupangProductRepository.findByUserIdAndSourceProductId(1L, "ASIN-001")).thenReturn(Optional.empty());
        when(coupangProductRepository.findByUserIdAndSourceProductId(1L, "ASIN-002")).thenReturn(Optional.empty());
        when(coupangProductRepository.save(any(DummyCoupangProduct.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        List<DummyCoupangProduct> products = service.publishAll(1L, List.of(10L, 11L));

        assertEquals(2, products.size());
        assertEquals(RegistrationStatus.REGISTERED, first.getRegistrationStatus());
        assertEquals(RegistrationStatus.REGISTERED, second.getRegistrationStatus());
    }

    @Test
    @DisplayName("쿠팡 더미 상품 상세 조회는 현재 로그인 사용자의 소유 데이터만 반환한다")
    void getProduct_returnsOwnedProduct() {
        DummyProductRegistration registration = createRegistration(10L, RegistrationStatus.REGISTERED, MarketCode.COUPANG);
        DummyCoupangProduct product = DummyCoupangProduct.create(registration);

        when(coupangProductRepository.findByDummyCoupangProductIdAndUserId(30L, 1L))
                .thenReturn(Optional.of(product));

        DummyCoupangProduct result = service.getProduct(1L, 30L);

        assertSame(product, result);
    }

    private DummyProductRegistration createRegistration(Long registrationId, RegistrationStatus status, MarketCode marketCode) {
        DummyProductRegistration registration = DummyProductRegistration.create(
                1L,
                marketCode,
                "ASIN-001",
                "https://amazon.com/dp/ASIN-001",
                "https://image.example/main.jpg",
                "가공 상품명",
                "브랜드",
                BigDecimal.TEN,
                "USD",
                BigDecimal.valueOf(1350),
                BigDecimal.valueOf(13500),
                BigDecimal.valueOf(22000),
                BigDecimal.valueOf(3000),
                status,
                null
        );
        ReflectionTestUtils.setField(registration, "dummyProductRegistrationId", registrationId);
        return registration;
    }
}

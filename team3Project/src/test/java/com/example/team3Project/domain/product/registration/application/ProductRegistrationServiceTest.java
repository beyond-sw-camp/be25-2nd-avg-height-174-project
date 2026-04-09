package com.example.team3Project.domain.product.registration.application;

import com.example.team3Project.domain.policy.entity.MarketCode;
import com.example.team3Project.domain.product.registration.dao.DummyProductRegistrationRepository;
import com.example.team3Project.domain.product.registration.entity.DummyProductRegistration;
import com.example.team3Project.domain.product.registration.entity.RegistrationStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductRegistrationServiceTest {

    private DummyProductRegistrationRepository repository;
    private ProductRegistrationService service;

    @BeforeEach
    void setUp() {
        repository = mock(DummyProductRegistrationRepository.class);
        service = new ProductRegistrationService(repository, new ObjectMapper());
    }

    @Test
    @DisplayName("같은 사용자와 같은 상품이 다시 등록되면 기존 등록 건을 갱신한다")
    void register_updatesExistingRegistration_whenSameUserAndSourceProduct() {
        DummyProductRegistration existing = DummyProductRegistration.create(
                1L,
                MarketCode.COUPANG,
                "ASIN-001",
                "old-url",
                "old-main",
                "old-name",
                "old-brand",
                BigDecimal.valueOf(10),
                "USD",
                BigDecimal.valueOf(1300),
                BigDecimal.valueOf(13000),
                BigDecimal.valueOf(20000),
                BigDecimal.valueOf(3000),
                RegistrationStatus.READY,
                null
        );
        ReflectionTestUtils.setField(existing, "dummyProductRegistrationId", 99L);

        when(repository.findByUserIdAndMarketCodeAndSourceProductId(1L, MarketCode.COUPANG, "ASIN-001"))
                .thenReturn(Optional.of(existing));
        when(repository.save(existing)).thenReturn(existing);

        DummyProductRegistration result = service.register(
                1L,
                MarketCode.COUPANG,
                "ASIN-001",
                "new-url",
                "new-main",
                List.of("desc-1"),
                List.of(),
                "new-name",
                "new-brand",
                BigDecimal.valueOf(20),
                "USD",
                BigDecimal.valueOf(1350),
                BigDecimal.valueOf(27000),
                BigDecimal.valueOf(35000),
                BigDecimal.valueOf(4000),
                RegistrationStatus.BLOCKED,
                "BLOCKED_WORD"
        );

        assertSame(existing, result);
        assertEquals("new-url", existing.getSourceUrl());
        assertEquals("new-name", existing.getProcessedProductName());
        assertEquals(RegistrationStatus.BLOCKED, existing.getRegistrationStatus());
        verify(repository).save(existing);
    }

    @Test
    @DisplayName("등록 삭제는 로그인 사용자 소유 데이터만 삭제한다")
    void deleteRegistration_deletesOwnedRegistration() {
        DummyProductRegistration registration = DummyProductRegistration.create(
                1L,
                MarketCode.COUPANG,
                "ASIN-001",
                "url",
                "main",
                "name",
                "brand",
                BigDecimal.TEN,
                "USD",
                BigDecimal.valueOf(1350),
                BigDecimal.valueOf(13500),
                BigDecimal.valueOf(20000),
                BigDecimal.valueOf(3000),
                RegistrationStatus.READY,
                null
        );

        when(repository.findByDummyProductRegistrationIdAndUserId(10L, 1L))
                .thenReturn(Optional.of(registration));

        service.deleteRegistration(1L, 10L);

        verify(repository).delete(registration);
    }

    @Test
    @DisplayName("등록 삭제 대상이 없으면 404를 반환한다")
    void deleteRegistration_throwsNotFound_whenRegistrationMissing() {
        when(repository.findByDummyProductRegistrationIdAndUserId(10L, 1L))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.deleteRegistration(1L, 10L)
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    @Test
    @DisplayName("등록 다건 삭제는 선택한 항목이 모두 로그인 사용자 소유일 때만 삭제한다")
    void deleteRegistrations_deletesSelectedRegistrations() {
        DummyProductRegistration first = DummyProductRegistration.create(
                1L, MarketCode.COUPANG, "ASIN-001", "url1", "main1", "name1", "brand1",
                BigDecimal.TEN, "USD", BigDecimal.valueOf(1350), BigDecimal.valueOf(13500),
                BigDecimal.valueOf(20000), BigDecimal.valueOf(3000), RegistrationStatus.READY, null
        );
        DummyProductRegistration second = DummyProductRegistration.create(
                1L, MarketCode.COUPANG, "ASIN-002", "url2", "main2", "name2", "brand2",
                BigDecimal.TEN, "USD", BigDecimal.valueOf(1350), BigDecimal.valueOf(13500),
                BigDecimal.valueOf(21000), BigDecimal.valueOf(3000), RegistrationStatus.READY, null
        );

        when(repository.findAllByDummyProductRegistrationIdInAndUserId(List.of(10L, 11L), 1L))
                .thenReturn(List.of(first, second));

        service.deleteRegistrations(1L, List.of(10L, 11L));

        verify(repository).deleteAll(List.of(first, second));
    }

    @Test
    @DisplayName("등록 다건 삭제 대상 중 일부라도 없으면 404를 반환한다")
    void deleteRegistrations_throwsNotFound_whenSomeRegistrationsMissing() {
        DummyProductRegistration first = DummyProductRegistration.create(
                1L, MarketCode.COUPANG, "ASIN-001", "url1", "main1", "name1", "brand1",
                BigDecimal.TEN, "USD", BigDecimal.valueOf(1350), BigDecimal.valueOf(13500),
                BigDecimal.valueOf(20000), BigDecimal.valueOf(3000), RegistrationStatus.READY, null
        );

        when(repository.findAllByDummyProductRegistrationIdInAndUserId(List.of(10L, 11L), 1L))
                .thenReturn(List.of(first));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.deleteRegistrations(1L, List.of(10L, 11L))
        );

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }
}

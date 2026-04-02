package com.example.team3Project.domain.product.registration.application;

import com.example.team3Project.domain.policy.entity.MarketCode;
import com.example.team3Project.domain.product.registration.dao.DummyProductRegistrationRepository;
import com.example.team3Project.domain.product.registration.entity.DummyProductRegistration;
import com.example.team3Project.domain.product.registration.entity.RegistrationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
// 등록 저장 전용 서비스
public class ProductRegistrationService {

    private final DummyProductRegistrationRepository dummyProductRegistrationRepository;

    public DummyProductRegistration register(
            Long userId,
            MarketCode marketCode,
            String sourceProductId,
            String sourceUrl,
            String processedProductName,
            String processedBrand,
            BigDecimal originalPrice,
            String currency,
            BigDecimal exchangeRate,
            BigDecimal costInKrw,
            BigDecimal salePrice,
            BigDecimal shippingFee,
            RegistrationStatus registrationStatus,
            String exclusionReason
    ) {
        DummyProductRegistration registration = DummyProductRegistration.create(
                userId,
                marketCode,
                sourceProductId,
                sourceUrl,
                processedProductName,
                processedBrand,
                originalPrice,
                currency,
                exchangeRate,
                costInKrw,
                salePrice,
                shippingFee,
                registrationStatus,
                exclusionReason
        );

        return dummyProductRegistrationRepository.save(registration);
    }

    // 사용자 마켓별 등록 목록 조회용 메서드
    @Transactional(readOnly = true)
    public List<DummyProductRegistration> getRegistrations(Long userId, MarketCode marketCode) {
        return dummyProductRegistrationRepository.findByUserIdAndMarketCode(userId, marketCode);
    }

    // 사용자 마켓별 등록 건별 조회용 메서드
    @Transactional(readOnly = true)
    public DummyProductRegistration getRegistration(Long registrationId) {
        return dummyProductRegistrationRepository.findById(registrationId)
                .orElseThrow(() -> new IllegalArgumentException("등록 상품을 찾을 수 없습니다. id=" + registrationId));
    }

}

package com.example.team3Project.domain.product.registration.entity;

import com.example.team3Project.domain.policy.entity.MarketCode;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "dummy_product_registration")
@Getter
@NoArgsConstructor
public class DummyProductRegistration {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dummy_product_registration_id")
    private Long dummyProductRegistrationId;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    @Column(name = "market_code", nullable = false)
    private MarketCode marketCode;

    @Column(name = "source_product_id", nullable = false)
    private String sourceProductId;

    @Column(name = "source_url", nullable = false)
    private String sourceUrl;

    @Column(name = "processed_product_name", nullable = false)
    private String processedProductName;

    @Column(name = "processed_brand", nullable = false)
    private String processedBrand;

    @Column(name = "original_price", nullable = false)
    private BigDecimal originalPrice;

    @Column(name = "currency", nullable = false)
    private String currency;

    @Column(name = "exchange_rate", nullable = false)
    private BigDecimal exchangeRate;

    @Column(name = "cost_in_krw", nullable = false)
    private BigDecimal costInKrw;

    @Column(name = "sale_price", nullable = false)
    private BigDecimal salePrice;

    @Column(name = "shipping_fee", nullable = false)
    private BigDecimal shippingFee;

    @Enumerated(EnumType.STRING)
    @Column(name = "registration_status", nullable = false)
    private RegistrationStatus registrationStatus;

    @Column(name = "exclusion_reason")
    private String exclusionReason;

    public static DummyProductRegistration create(
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
        DummyProductRegistration registration = new DummyProductRegistration();
        registration.userId = userId;
        registration.marketCode = marketCode;
        registration.sourceProductId = sourceProductId;
        registration.sourceUrl = sourceUrl;
        registration.processedProductName = processedProductName;
        registration.processedBrand = processedBrand;
        registration.originalPrice = originalPrice;
        registration.currency = currency;
        registration.exchangeRate = exchangeRate;
        registration.costInKrw = costInKrw;
        registration.salePrice = salePrice;
        registration.shippingFee = shippingFee;
        registration.registrationStatus = registrationStatus;
        registration.exclusionReason = exclusionReason;
        return registration;
    }

    public void update(
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
        this.sourceUrl = sourceUrl;
        this.processedProductName = processedProductName;
        this.processedBrand = processedBrand;
        this.originalPrice = originalPrice;
        this.currency = currency;
        this.exchangeRate = exchangeRate;
        this.costInKrw = costInKrw;
        this.salePrice = salePrice;
        this.shippingFee = shippingFee;
        this.registrationStatus = registrationStatus;
        this.exclusionReason = exclusionReason;
    }
}

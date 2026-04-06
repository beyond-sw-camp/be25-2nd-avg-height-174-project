package com.example.team3Project.domain.product.registration.entity;

import com.example.team3Project.domain.policy.entity.MarketCode;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "dummy_product_registration")
@Getter
@NoArgsConstructor
// 더미 - 상품 등록 엔터티
// 상품 1건
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

    @Column(name = "main_image_url")
    private String mainImageUrl;

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

    @JsonManagedReference("registration-options")
    @OneToMany(mappedBy = "registration", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DummyProductOption> options = new ArrayList<>();

    @JsonManagedReference("registration-images")
    @OneToMany(mappedBy = "registration", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<DummyProductImage> images = new ArrayList<>();

    public static DummyProductRegistration create(
            Long userId,
            MarketCode marketCode,
            String sourceProductId,
            String sourceUrl,
            String mainImageUrl,
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
        registration.mainImageUrl = mainImageUrl;
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
            String mainImageUrl,
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
        this.mainImageUrl = mainImageUrl;
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

    public void replaceOptions(List<DummyProductOption> options) {
        this.options.clear();
        options.forEach(this::addOption);
    }

    public void replaceImages(List<DummyProductImage> images) {
        this.images.clear();
        images.forEach(this::addImage);
    }

    public void addOption(DummyProductOption option) {
        option.assignRegistration(this);
        this.options.add(option);
    }

    public void addImage(DummyProductImage image) {
        image.assignRegistration(this);
        this.images.add(image);
    }
}

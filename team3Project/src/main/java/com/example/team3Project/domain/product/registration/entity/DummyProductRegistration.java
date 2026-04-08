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

    // 객체를 JSON으로 바꿀 때 생기는 순환참조 문제를 해결하기 위한 어노테이션
    // JSON에 작성이 된다.
    @JsonManagedReference("registration-options")
    // 1: N관계(registration 1개가 여러개의 registrationOption을 갖는다.)
    @OneToMany(mappedBy = "registration", cascade = CascadeType.ALL, orphanRemoval = true)
    // 소싱 요청의 variation 목록을 등록 상품의 옵션 목록으로 저장한다.
    private List<DummyProductOption> options = new ArrayList<>();

    @JsonManagedReference("registration-images")
    // 1: N관계(registration 1개가 여러개의 registrationImage를 갖는다.)
    @OneToMany(mappedBy = "registration", cascade = CascadeType.ALL, orphanRemoval = true)
    // 대표 이미지와 옵션 이미지를 함께 보관하고 enum으로 구분할 수 있게 한다.
    private List<DummyProductImage> images = new ArrayList<>();

    // 상품 1건 더미 등록 시 객체 생성
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

    // 더미 상품 1건 수정
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

    // 상품 1건 옵션 수정
    public void replaceOptions(List<DummyProductOption> options) {
        this.options.clear(); // options 리스트의 요소를 전부 비운다.
        options.forEach(this::addOption); // 새 options에 연관관계를 부여한다.
    }

    // 상품 1건 이미지 수정
    public void replaceImages(List<DummyProductImage> images) {
        this.images.clear(); // images 리스트의 요소를 전부 비운다.
        images.forEach(this::addImage); // 새 images에 연관관계를 부여한다.
    }

    public void addOption(DummyProductOption option) {
        option.assignRegistration(this); // 옵션을 현재 상품 등록 건에 소속시킨다.
        this.options.add(option); // option 리스트에 추가
    }

    public void addImage(DummyProductImage image) {
        image.assignRegistration(this); // 이미지를 현재 상품 등록 건에 소속시킨다.
        this.images.add(image); // 이미지 리스트에 추가
    }
}

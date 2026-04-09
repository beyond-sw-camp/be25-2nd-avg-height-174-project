package com.example.team3Project.domain.product.coupang.entity;

import com.example.team3Project.domain.product.registration.entity.DummyProductImageType;
import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "dummy_coupang_product_image")
@Getter
@NoArgsConstructor
// 쿠팡 더미 상품의 메인/상세/옵션 이미지를 저장한다.
public class DummyCoupangProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dummy_coupang_product_image_id")
    private Long dummyCoupangProductImageId;

    @JsonBackReference("coupang-product-images")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dummy_coupang_product_id", nullable = false)
    private DummyCoupangProduct product;

    @Enumerated(EnumType.STRING)
    @Column(name = "image_type", nullable = false, length = 30)
    private DummyProductImageType imageType;

    @Column(name = "option_asin")
    private String optionAsin;

    @Column(name = "image_url", nullable = false, length = 1000)
    private String imageUrl;

    @Column(name = "sort_order")
    private Integer sortOrder;

    // 등록 후보 이미지를 쿠팡 더미 이미지로 복사한다.
    public static DummyCoupangProductImage create(
            DummyProductImageType imageType,
            String optionAsin,
            String imageUrl,
            Integer sortOrder
    ) {
        DummyCoupangProductImage image = new DummyCoupangProductImage();
        image.imageType = imageType;
        image.optionAsin = optionAsin;
        image.imageUrl = imageUrl;
        image.sortOrder = sortOrder;
        return image;
    }

    void assignProduct(DummyCoupangProduct product) {
        this.product = product;
    }
}

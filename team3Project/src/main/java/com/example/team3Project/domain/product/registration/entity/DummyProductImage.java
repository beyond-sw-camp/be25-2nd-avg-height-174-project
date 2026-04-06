package com.example.team3Project.domain.product.registration.entity;

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
@Table(name = "dummy_product_image")
@Getter
@NoArgsConstructor
public class DummyProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dummy_product_image_id")
    private Long dummyProductImageId;

    @JsonBackReference("registration-images")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dummy_product_registration_id", nullable = false)
    private DummyProductRegistration registration;

    @Enumerated(EnumType.STRING)
    @Column(name = "image_type", nullable = false, length = 30)
    private DummyProductImageType imageType;

    @Column(name = "option_asin")
    private String optionAsin;

    @Column(name = "image_url", nullable = false, length = 1000)
    private String imageUrl;

    @Column(name = "sort_order")
    private Integer sortOrder;

    public static DummyProductImage create(
            DummyProductImageType imageType,
            String optionAsin,
            String imageUrl,
            Integer sortOrder
    ) {
        DummyProductImage image = new DummyProductImage();
        image.imageType = imageType;
        image.optionAsin = optionAsin;
        image.imageUrl = imageUrl;
        image.sortOrder = sortOrder;
        return image;
    }

    void assignRegistration(DummyProductRegistration registration) {
        this.registration = registration;
    }
}

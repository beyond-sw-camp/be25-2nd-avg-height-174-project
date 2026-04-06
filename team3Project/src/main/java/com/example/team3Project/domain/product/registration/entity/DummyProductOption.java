package com.example.team3Project.domain.product.registration.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Table(name = "dummy_product_option")
@Getter
@NoArgsConstructor
public class DummyProductOption {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "dummy_product_option_id")
    private Long dummyProductOptionId;

    @JsonBackReference("registration-options")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dummy_product_registration_id", nullable = false)
    private DummyProductRegistration registration;

    @Column(name = "option_asin", nullable = false)
    private String optionAsin;

    @Lob
    @Column(name = "option_dimensions")
    private String optionDimensions;

    @Column(name = "selected", nullable = false)
    private boolean selected;

    @Column(name = "price")
    private BigDecimal price;

    @Column(name = "currency", length = 10)
    private String currency;

    @Column(name = "stock", length = 1000)
    private String stock;

    @Column(name = "rating")
    private Double rating;

    @Column(name = "reviews_count")
    private Integer reviewsCount;

    public static DummyProductOption create(
            String optionAsin,
            String optionDimensions,
            boolean selected,
            BigDecimal price,
            String currency,
            String stock,
            Double rating,
            Integer reviewsCount
    ) {
        DummyProductOption option = new DummyProductOption();
        option.optionAsin = optionAsin;
        option.optionDimensions = optionDimensions;
        option.selected = selected;
        option.price = price;
        option.currency = currency;
        option.stock = stock;
        option.rating = rating;
        option.reviewsCount = reviewsCount;
        return option;
    }

    void assignRegistration(DummyProductRegistration registration) {
        this.registration = registration;
    }
}

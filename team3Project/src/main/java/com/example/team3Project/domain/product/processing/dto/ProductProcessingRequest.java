package com.example.team3Project.domain.product.processing.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@NoArgsConstructor
// 소싱 상품 가공 요청 DTO
public class ProductProcessingRequest {

    // 소싱의 ASIN(AmazonStandardIdentificationNumber)
    @NotBlank
    private String sourceProductId;

    // 소싱의 url
    @NotBlank
    private String sourceUrl;

    // 소싱의 translatedTitle
    @NotBlank
    private String translatedProductName;

    // 소싱의 translatedBrand
    @NotBlank
    private String translatedBrand;

    // 소싱의 price
    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal originalPrice;

    // 소싱의 currency - 통화 코드(ex.USD)
    @NotBlank
    private String currency;

    // 소싱의 url_image
    @NotBlank
    private String mainImageUrl;

    // 소싱의 images
    @NotEmpty
    private List<String> descriptionImageUrls;
}

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

    // 소싱 서비스 응답을 받아서 가공 흐름으로 넘길 때 사용할 객체
    // 정적 팩토리 메서드 - 클래스가 자기 자신을 만들어서 반환하는 static 메서드
    // 생성자 로직을 숨길 수 있음
    public static ProductProcessingRequest of(
            String sourceProductId,
            String sourceUrl,
            String translatedProductName,
            String translatedBrand,
            BigDecimal originalPrice,
            String currency,
            String mainImageUrl,
            List<String> descriptionImageUrls
    ) {
        ProductProcessingRequest request = new ProductProcessingRequest();
        request.sourceProductId = sourceProductId;
        request.sourceUrl = sourceUrl;
        request.translatedProductName = translatedProductName;
        request.translatedBrand = translatedBrand;
        request.originalPrice = originalPrice;
        request.currency = currency;
        request.mainImageUrl = mainImageUrl;
        request.descriptionImageUrls = descriptionImageUrls;
        return request;
    }
}

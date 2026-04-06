package com.example.team3Project.domain.product.processing.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

@Getter
@NoArgsConstructor
// 소싱 API의 variation 한 개를 담는 DTO 클래스
public class SourcingVariationResponse {
    @JsonProperty("asin")
    private String asin;

    // 옵션 속성들을 저장
    // 예) Flavor Name-Coke Zero, Size-12 Fl Oz
    @JsonProperty("dimensions")
    private Map<String, String> dimensions;

    @JsonProperty("selected")
    private boolean selected;

    // 옵션별 가격
    @JsonProperty("price")
    private BigDecimal price;

    @JsonProperty("currency")
    private String currency;

    // 재고 상태
    @JsonProperty("stock")
    private String stock;

    @JsonProperty("rating")
    private Double rating;

    @JsonProperty("reviews_count")
    private Integer reviewsCount;

    // 옵션별 이미지 url 목록
    @JsonProperty("images")
    private List<String> images;
}

package com.example.team3Project.domain.product.processing.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Getter
@NoArgsConstructor  // Json을 객체로 바안딩할 때 사용할 기본 생성자
// 소싱 서비스 API 응답 전체를 담는 DTO 클래스
public class SourcingProductResponse {

    // 아마존 상품 식별값
    @JsonProperty("asin")
    private String asin;

    // 원본 브랜드명
    @JsonProperty("brand")
    private String brand;

    // 통화 코드
    @JsonProperty("currency")
    private String currency;

    // 상품 원가
    @JsonProperty("price")
    private BigDecimal price;

    // 상품 평점
    @JsonProperty("rating")
    private Double rating;

    // 리뷰 수
    @JsonProperty("reviews_count")
    private Integer reviewsCount;

    // 판매량
    @JsonProperty("sales_volume")
    private String salesVolume;

    // 배송 관련 문자열
    @JsonProperty("shipping_information")
    private String shippingInformation;

    // 상품명
    @JsonProperty("title")
    private String title;

    // 소싱 원본 Url
    @JsonProperty("url")
    private String url;

    // 대표 이미지 Url
    @JsonProperty("url_image")
    private String urlImage;

    // 상세 이미지 Url 목록
    @JsonProperty("images")
    private List<String> images;

    // 옵션 목록
    @JsonProperty("variation")
    private List<SourcingVariationResponse> variation;
}
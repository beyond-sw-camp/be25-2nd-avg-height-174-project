package com.example.team3Project.domain.product.processing.dto;

import com.example.team3Project.domain.policy.entity.MarketCode;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.Valid;
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
@JsonIgnoreProperties(ignoreUnknown = true)
public class SourcingCompletedRequest {

    @NotNull
    private Long userId;

    @NotNull
    private MarketCode marketCode;

    @NotBlank
    @JsonProperty("asin")
    private String asin;

    @NotBlank
    @JsonProperty("brand")
    private String brand;

    @NotBlank
    @JsonProperty("currency")
    private String currency;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    @JsonProperty("price")
    private BigDecimal price;

    @NotBlank
    @JsonProperty("title")
    private String title;

    @NotBlank
    @JsonProperty("url")
    private String url;

    @NotBlank
    @JsonProperty("url_image")
    private String urlImage;

    @NotEmpty
    @JsonProperty("images")
    private List<String> images;

    @Valid
    @NotNull
    @JsonProperty("variation")
    private List<SourcingVariationResponse> variation;
}

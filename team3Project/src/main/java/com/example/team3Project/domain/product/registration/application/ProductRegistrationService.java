package com.example.team3Project.domain.product.registration.application;

import com.example.team3Project.domain.policy.entity.MarketCode;
import com.example.team3Project.domain.product.processing.dto.SourcingVariationResponse;
import com.example.team3Project.domain.product.registration.dao.DummyProductRegistrationRepository;
import com.example.team3Project.domain.product.registration.entity.DummyProductImage;
import com.example.team3Project.domain.product.registration.entity.DummyProductImageType;
import com.example.team3Project.domain.product.registration.entity.DummyProductOption;
import com.example.team3Project.domain.product.registration.entity.DummyProductRegistration;
import com.example.team3Project.domain.product.registration.entity.RegistrationStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Transactional
// 등록 저장 전용 서비스
public class ProductRegistrationService {

    private final DummyProductRegistrationRepository dummyProductRegistrationRepository;
    private final ObjectMapper objectMapper;

    public DummyProductRegistration register(
            Long userId,
            MarketCode marketCode,
            String sourceProductId,
            String sourceUrl,
            String mainImageUrl,
            List<String> descriptionImageUrls,
            List<SourcingVariationResponse> sourcingVariations,
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
        DummyProductRegistration registration = DummyProductRegistration.create(
                userId,
                marketCode,
                sourceProductId,
                sourceUrl,
                mainImageUrl,
                processedProductName,
                processedBrand,
                originalPrice,
                currency,
                exchangeRate,
                costInKrw,
                salePrice,
                shippingFee,
                registrationStatus,
                exclusionReason
        );

        // 소싱 variation 원본을 현재 더미 등록의 옵션 엔티티 목록으로 바꿔서 저장한다.
        registration.replaceOptions(toDummyOptions(sourcingVariations));
        // 대표 이미지, 설명 이미지, 옵션 이미지를 하나의 이미지 컬렉션으로 모아 저장한다.
        registration.replaceImages(toDummyImages(mainImageUrl, descriptionImageUrls, sourcingVariations));

        return dummyProductRegistrationRepository.save(registration);
    }

    // 사용자/마켓별 등록 목록 조회용 메서드
    @Transactional(readOnly = true)
    public List<DummyProductRegistration> getRegistrations(Long userId, MarketCode marketCode) {
        return dummyProductRegistrationRepository.findByUserIdAndMarketCode(userId, marketCode);
    }

    // 사용자/마켓별 등록 단건 조회용 메서드
    @Transactional(readOnly = true)
    public DummyProductRegistration getRegistration(Long registrationId) {
        return dummyProductRegistrationRepository.findById(registrationId)
                .orElseThrow(() -> new IllegalArgumentException("등록 상품을 찾을 수 없습니다. id=" + registrationId));
    }

    private List<DummyProductOption> toDummyOptions(List<SourcingVariationResponse> sourcingVariations) {
        List<DummyProductOption> options = new ArrayList<>();
        if (sourcingVariations == null) {
            return options;
        }

        for (SourcingVariationResponse variation : sourcingVariations) {
            options.add(
                    DummyProductOption.create(
                            variation.getAsin(),
                            toDimensionsJson(variation.getDimensions()),
                            variation.isSelected(),
                            variation.getPrice(),
                            variation.getCurrency(),
                            variation.getStock(),
                            // 평점과 리뷰 수는 현재 저장 범위에서 제외하고 이후 확장 대상으로 남겨 둔다.
                            null,
                            null
                    )
            );
        }
        return options;
    }

    private List<DummyProductImage> toDummyImages(
            String mainImageUrl,
            List<String> descriptionImageUrls,
            List<SourcingVariationResponse> sourcingVariations
    ) {
        List<DummyProductImage> images = new ArrayList<>();

        if (mainImageUrl != null && !mainImageUrl.isBlank()) {
            images.add(DummyProductImage.create(DummyProductImageType.MAIN, null, mainImageUrl, 0));
        }

        if (descriptionImageUrls != null) {
            for (int i = 0; i < descriptionImageUrls.size(); i++) {
                images.add(
                        DummyProductImage.create(
                                DummyProductImageType.DESCRIPTION,
                                null,
                                descriptionImageUrls.get(i),
                                i
                        )
                );
            }
        }

        if (sourcingVariations != null) {
            for (SourcingVariationResponse variation : sourcingVariations) {
                List<String> optionImages = variation.getImages();
                if (optionImages == null) {
                    continue;
                }
                for (int i = 0; i < optionImages.size(); i++) {
                    images.add(
                            DummyProductImage.create(
                                    DummyProductImageType.OPTION,
                                    variation.getAsin(),
                                    optionImages.get(i),
                                    i
                            )
                    );
                }
            }
        }

        return images;
    }

    private String toDimensionsJson(Map<String, String> dimensions) {
        if (dimensions == null || dimensions.isEmpty()) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(dimensions);
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("옵션 속성 정보를 문자열로 변환할 수 없습니다.", e);
        }
    }
}

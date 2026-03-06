package com.example.team3Project.domain.sourcing;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SourcingService {

    private static final Set<String> VALID_CURRENCIES = Set.of(
        "KRW", "USD", "JPY", "CNY", "EUR", "GBP", "HKD", "SGD", "AUD", "CAD"
    );

    private static final Set<String> VALID_STOCK_STATUSES = Set.of(
        "in_stock", "out_of_stock", "limited", "unknown"
    );

    // mariaDB에 저장하기 위한 하나의 객체.
    private final SourcingRepository sourcingRepository;

    // 누락/비정상 데이터 필터링
    public List<String> validateSourcingData(SourcingDTO sourcingDTO) {
        List<String> errors = new ArrayList<>();

        // 누락 검사
        if (!StringUtils.hasText(sourcingDTO.getSourceUrl())) {
            errors.add("[누락] sourceUrl이 누락 되었습니다.");
        } else if (!isValidUrl(sourcingDTO.getSourceUrl())) {
            errors.add("[비정상] sourceUrl 형식이 올바르지 않습니다: " + sourcingDTO.getSourceUrl());
        }

        if (!StringUtils.hasText(sourcingDTO.getProductId())) {
            errors.add("[누락] productId가 누락 되었습니다.");
        }

        if (sourcingDTO.getData() == null) {
            errors.add("[누락] data의 정보가 누락 되었습니다.");
            return errors;
        }

        SourcingDTO.ProductData productData = sourcingDTO.getData();

        // title 누락 검사
        if (!StringUtils.hasText(productData.getTitle())) {
            errors.add("[누락] title이 누락 되었습니다.");
        }

        // originalPrice 누락/비정상 검사
        if (productData.getOriginalPrice() == null) {
            errors.add("[누락] originalPrice가 누락 되었습니다.");
        } else if (productData.getOriginalPrice().compareTo(BigDecimal.ZERO) <= 0) {
            errors.add("[비정상] originalPrice는 0보다 커야 합니다: " + productData.getOriginalPrice());
        }

        // currency 누락/비정상 검사
        if (!StringUtils.hasText(productData.getCurrency())) {
            errors.add("[누락] currency가 누락 되었습니다.");
        } else if (!VALID_CURRENCIES.contains(productData.getCurrency().toUpperCase())) {
            errors.add("[비정상] 지원하지 않는 currency입니다: " + productData.getCurrency()
                    + " (지원: " + VALID_CURRENCIES + ")");
        }

        // brand 누락 검사
        if (!StringUtils.hasText(productData.getBrand())) {
            errors.add("[누락] brand가 누락 되었습니다.");
        }

        // mainImageUrl 누락/비정상 검사
        if (!StringUtils.hasText(productData.getMainImageUrl())) {
            errors.add("[누락] mainImageUrl이 누락 되었습니다.");
        } else if (!isValidUrl(productData.getMainImageUrl())) {
            errors.add("[비정상] mainImageUrl 형식이 올바르지 않습니다: " + productData.getMainImageUrl());
        }

        // stockStatus 비정상 검사
        if (StringUtils.hasText(productData.getStockStatus())
                && !VALID_STOCK_STATUSES.contains(productData.getStockStatus().toLowerCase())) {
            errors.add("[비정상] 지원하지 않는 stockStatus입니다: " + productData.getStockStatus()
                    + " (지원: " + VALID_STOCK_STATUSES + ")");
        }

        // options 누락/비정상 검사
        if (productData.getOptions() == null || productData.getOptions().isEmpty()) {
            errors.add("[누락] options가 누락 되었습니다.");
        } else {
            for (int i = 0; i < productData.getOptions().size(); i++) {
                SourcingDTO.Option option = productData.getOptions().get(i);
                if (!StringUtils.hasText(option.getName())) {
                    errors.add("[누락] options[" + i + "].name이 누락 되었습니다.");
                }
                if (option.getValues() == null || option.getValues().isEmpty()) {
                    errors.add("[누락] options[" + i + "].values가 누락 되었습니다.");
                }
            }
        }

        return errors;
    }

    // 여러 상품 일괄 검증
    public ValidationResultDTO validateBatch(List<SourcingDTO> items) {
        List<ValidationResultDTO.ItemResult> validItems = new ArrayList<>();
        List<ValidationResultDTO.ItemResult> invalidItems = new ArrayList<>();

        for (int i = 0; i < items.size(); i++) {
            SourcingDTO dto = items.get(i);
            List<String> errors = validateSourcingData(dto);
            String productId = dto.getProductId() != null ? dto.getProductId() : "item[" + i + "]";

            if (errors.isEmpty()) {
                validItems.add(new ValidationResultDTO.ItemResult(productId, errors));
            } else {
                invalidItems.add(new ValidationResultDTO.ItemResult(productId, errors));
            }
        }

        return new ValidationResultDTO(validItems, invalidItems);
    }

    private boolean isValidUrl(String url) {
        return url != null && (url.startsWith("http://") || url.startsWith("https://"));
    }

    @Transactional
    public void saveSourcingData(SourcingDTO sourcingDTO) {
        Sourcing sourcing = Sourcing.builder()
                .sourceUrl(sourcingDTO.getSourceUrl())
                .siteName(sourcingDTO.getSiteName())
                .productId(sourcingDTO.getProductId())
                .collectedAt(sourcingDTO.getCollectedAt())
                .title(sourcingDTO.getData().getTitle())
                .originalPrice(sourcingDTO.getData().getOriginalPrice())
                .currency(sourcingDTO.getData().getCurrency())
                .brand(sourcingDTO.getData().getBrand())
                .mainImageUrl(sourcingDTO.getData().getMainImageUrl())
                .stockStatus(sourcingDTO.getData().getStockStatus())
                .build();

        sourcingRepository.save(sourcing);
        
    }

}
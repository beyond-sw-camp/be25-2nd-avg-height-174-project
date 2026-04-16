package com.example.team3Project.domain.product.application;

import com.example.team3Project.domain.product.dao.DummyCoupangProduct;
import com.example.team3Project.domain.product.dao.DummyCoupangProductImage;
import com.example.team3Project.domain.product.dao.DummyCoupangProductImage.ImageType;
import com.example.team3Project.domain.product.dao.DummyCoupangProductOption;
import com.example.team3Project.domain.product.dao.DummyCoupangProductRepository;
import com.example.team3Project.domain.product.dto.ProductPageDto;
import com.example.team3Project.domain.product.dto.ProductPageDto.OptionDto;
import com.example.team3Project.domain.product.dto.ProductPageDto.ProductSummaryDto;
import io.minio.GetPresignedObjectUrlArgs;
import io.minio.MinioClient;
import io.minio.http.Method;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

// 상품 페이지 데이터 조회 및 이미지 URL 처리 서비스
@Slf4j
@Service
@RequiredArgsConstructor
public class ProductPageService {

    private final DummyCoupangProductRepository productRepository;
    private final MinioClient minioClient;

    @Value("${minio.url}")
    private String minioUrl;

    @Value("${minio.bucket}")
    private String minioBucket;

    // 특정 상품의 페이지 데이터(이미지, 옵션, 전체 상품 목록) 조합
    @Transactional(readOnly = true)
    public ProductPageDto getProductPage(Long productId) {
        DummyCoupangProduct product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("상품을 찾을 수 없습니다: " + productId));

        List<DummyCoupangProductImage> images = product.getImages();
        List<DummyCoupangProductOption> options = product.getOptions();

        // 이미지 타입별로 분류
        List<String> mainImageUrls = images.stream()
                .filter(img -> img.getImageType() == ImageType.MAIN)
                .map(this::resolveImageUrl)
                .filter(url -> url != null && !url.isBlank())
                .collect(Collectors.toList());

        List<String> descriptionImageUrls = images.stream()
                .filter(img -> img.getImageType() == ImageType.DESCRIPTION)
                .map(this::resolveImageUrl)
                .filter(url -> url != null && !url.isBlank())
                .collect(Collectors.toList());

        // OPTION 이미지를 optionAsin 기준으로 매핑
        Map<String, String> optionImageMap = images.stream()
                .filter(img -> img.getImageType() == ImageType.OPTION && img.getOptionAsin() != null)
                .collect(Collectors.toMap(
                        DummyCoupangProductImage::getOptionAsin,
                        this::resolveImageUrl,
                        (a, b) -> a
                ));

        // MAIN 이미지가 없으면 product.mainImageUrl 사용
        if (mainImageUrls.isEmpty() && product.getMainImageUrl() != null) {
            mainImageUrls.add(product.getMainImageUrl());
        }

        List<OptionDto> optionDtos = options.stream()
                .map(opt -> OptionDto.builder()
                        .optionId(opt.getId())
                        .optionAsin(opt.getOptionAsin())
                        .optionDimensions(parseOptionDimensions(opt.getOptionDimensions()))
                        .price(opt.getPrice())
                        .selected(Boolean.TRUE.equals(opt.getSelected()))
                        .stock(opt.getStock())
                        .currency(opt.getCurrency())
                        .imageUrl(optionImageMap.getOrDefault(opt.getOptionAsin(), ""))
                        .build())
                .collect(Collectors.toList());

        // 좌측 사이드바용 전체 상품 요약 목록
        List<ProductSummaryDto> allProducts = productRepository.findAll().stream()
                .map(p -> ProductSummaryDto.builder()
                        .productId(p.getId())
                        .productName(p.getProductName())
                        .mainImageUrl(p.getMainImageUrl())
                        .salePrice(p.getSalePrice())
                        .build())
                .collect(Collectors.toList());

        return ProductPageDto.builder()
                .productId(product.getId())
                .productName(product.getProductName())
                .brand(product.getBrand())
                .salePrice(product.getSalePrice())
                .originalPrice(product.getOriginalPrice())
                .shippingFee(product.getShippingFee())
                .mainImageUrls(mainImageUrls)
                .descriptionImageUrls(descriptionImageUrls)
                .options(optionDtos)
                .allProducts(allProducts)
                .build();
    }

    // DB에서 첫 번째 상품 ID 반환 (기본 리다이렉트 용도)
    @Transactional(readOnly = true)
    public Long getFirstProductId() {
        return productRepository.findAll().stream()
                .findFirst()
                .map(DummyCoupangProduct::getId)
                .orElseThrow(() -> new RuntimeException("등록된 상품이 없습니다."));
    }

    /**
     * 이미지 URL 해석
     * - object_key가 http로 시작하지 않으면 MinIO presigned URL 생성
     * - presigned URL 생성 실패 시 직접 MinIO URL로 fallback
     * - http URL이면 그대로 반환
     */
    private String resolveImageUrl(DummyCoupangProductImage image) {
        String objectKey = image.getObjectKey();
        String imageUrl = image.getImageUrl();

        if (objectKey != null && !objectKey.isBlank() && !objectKey.startsWith("http")) {
            try {
                return minioClient.getPresignedObjectUrl(
                        GetPresignedObjectUrlArgs.builder()
                                .method(Method.GET)
                                .bucket(minioBucket)
                                .object(objectKey)
                                .expiry(1, TimeUnit.DAYS)
                                .build()
                );
            } catch (Exception e) {
                log.warn("MinIO presigned URL 생성 실패 (직접 URL 사용): {}", objectKey, e);
                return minioUrl + "/" + minioBucket + "/" + objectKey;
            }
        }

        return imageUrl;
    }

    /**
     * JSON 형태의 옵션 dimensions를 사람이 읽기 좋은 문자열로 변환
     * 예: {"Flavor Name":"Coca-Cola","Size":"12 fl oz"} → Flavor Name: Coca-Cola / Size: 12 fl oz
     */
    private String parseOptionDimensions(String dimensions) {
        if (dimensions == null || dimensions.isBlank()) return "";
        try {
            String cleaned = dimensions.replaceAll("[{}\"]", "");
            return cleaned.replace(",", " / ");
        } catch (Exception e) {
            return dimensions;
        }
    }
}

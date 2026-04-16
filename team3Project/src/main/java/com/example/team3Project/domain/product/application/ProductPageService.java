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

    @Transactional(readOnly = true)
    public ProductPageDto getProductPage(Long productId) {
        DummyCoupangProduct product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

        List<DummyCoupangProductImage> images = product.getImages();
        List<DummyCoupangProductOption> options = product.getOptions();

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

        Map<String, String> optionImageMap = images.stream()
                .filter(img -> img.getImageType() == ImageType.OPTION && img.getOptionAsin() != null)
                .collect(Collectors.toMap(
                        DummyCoupangProductImage::getOptionAsin,
                        this::resolveImageUrl,
                        (first, second) -> first
                ));

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

    @Transactional(readOnly = true)
    public Long getFirstProductId() {
        return productRepository.findAll().stream()
                .findFirst()
                .map(DummyCoupangProduct::getId)
                .orElseThrow(() -> new IllegalStateException("No products are registered."));
    }

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
                log.warn("Failed to create MinIO presigned URL. Falling back to direct URL: {}", objectKey, e);
                return minioUrl + "/" + minioBucket + "/" + objectKey;
            }
        }

        return imageUrl;
    }

    private String parseOptionDimensions(String dimensions) {
        if (dimensions == null || dimensions.isBlank()) {
            return "";
        }

        try {
            String cleaned = dimensions.replaceAll("[{}\"]", "");
            return cleaned.replace(",", " / ");
        } catch (Exception e) {
            return dimensions;
        }
    }
}

package com.example.team3Project.domain.product.processing.api;

import com.example.team3Project.domain.policy.dto.ProductNameProcessingRequest;
import com.example.team3Project.domain.policy.dto.ProductNameProcessingResponse;
import com.example.team3Project.domain.policy.entity.MarketCode;
import com.example.team3Project.domain.product.processing.application.ProductProcessingService;
import com.example.team3Project.domain.product.processing.application.SourcingProductMapper;
import com.example.team3Project.domain.product.processing.dto.ProductProcessingRequest;
import com.example.team3Project.domain.product.processing.dto.ProductProcessingResultResponse;
import com.example.team3Project.domain.product.processing.dto.SourcingCompletedRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ProductProcessingController {

    // ProductProcessingService로 비즈니스 로직을 실행할 수 있도록 의존성 주입
    private final ProductProcessingService productProcessingService;
    private final SourcingProductMapper sourcingProductMapper;

    @PostMapping("/products/processing/name")
    // 상품명 가공 메서드
    public ResponseEntity<ProductNameProcessingResponse> processProductName(
            @RequestParam Long userId,
            @RequestParam MarketCode marketCode,
            @Valid @RequestBody ProductNameProcessingRequest request
    ) {
        // 서비스에서 상품명 가공 로직을 실행함
        ProductNameProcessingResponse response =
                productProcessingService.processProductNameResponse(userId, marketCode, request.getProductName());

        return ResponseEntity.ok(response);
    }

    // 상품 가공 메서드
    @PostMapping("/products/processing")
    public ResponseEntity<ProductProcessingResultResponse> processProduct(
            @RequestParam Long userId,
            @RequestParam MarketCode marketCode,
            @Valid @RequestBody ProductProcessingRequest request
    ) {
        ProductProcessingResultResponse response =
                productProcessingService.processProduct(userId, marketCode, request);

        return ResponseEntity.ok(response);

    }

    // 소싱 완료 요청 API
    @PostMapping("/api/sourcing/ingest")
    public ResponseEntity<ProductProcessingResultResponse> ingestSourcingProduct(
            @Valid @RequestBody SourcingCompletedRequest request
    ) {
        ProductProcessingRequest processingRequest = sourcingProductMapper.toProcessingRequest(request);
        ProductProcessingResultResponse response =
                productProcessingService.processProduct(request.getUserId(), request.getMarketCode(), processingRequest);

        return ResponseEntity.ok(response);
    }
}

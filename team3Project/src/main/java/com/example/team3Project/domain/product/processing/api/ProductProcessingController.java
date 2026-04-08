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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class ProductProcessingController {

    // 상품 가공 비즈니스 로직은 서비스에서 수행하고, 컨트롤러는 요청을 받아 전달하는 역할만 맡는다.
    private final ProductProcessingService productProcessingService;
    private final SourcingProductMapper sourcingProductMapper;

    @PostMapping("/products/processing/name")
    // 상품명만 먼저 가공해 보는 수동 호출 API
    public ResponseEntity<ProductNameProcessingResponse> processProductName(
            @RequestParam Long userId,
            @RequestParam MarketCode marketCode,
            @Valid @RequestBody ProductNameProcessingRequest request
    ) {
        ProductNameProcessingResponse response =
                productProcessingService.processProductNameResponse(userId, marketCode, request.getProductName());

        return ResponseEntity.ok(response);
    }

    @PostMapping("/products/processing")
    // 상품 1건 전체를 수동으로 가공하는 API
    public ResponseEntity<ProductProcessingResultResponse> processProduct(
            @RequestParam Long userId,
            @RequestParam MarketCode marketCode,
            @Valid @RequestBody ProductProcessingRequest request
    ) {
        ProductProcessingResultResponse response =
                productProcessingService.processProduct(userId, marketCode, request);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/api/sourcing/ingest")
    // 소싱 서비스가 상품 수집과 번역을 끝낸 뒤 내부적으로 호출하는 ingest 수신 API
    public ResponseEntity<ProductProcessingResultResponse> ingestSourcingProduct(
            @RequestHeader("X-User-Id") Long userId,
            @Valid @RequestBody SourcingCompletedRequest request
    ) {
        // 소싱 완료 요청 DTO를 현재 가공 서비스가 사용하는 내부 가공 요청 DTO로 변환한다.
        ProductProcessingRequest processingRequest = sourcingProductMapper.toProcessingRequest(request);
        ProductProcessingResultResponse response =
                // 사용자 식별은 payload 가 아니라 헤더의 X-User-Id 를 기준으로 정책 조회와 등록 저장을 수행한다.
                productProcessingService.processProduct(userId, request.getMarketCode(), processingRequest);

        return ResponseEntity.ok(response);
    }
}

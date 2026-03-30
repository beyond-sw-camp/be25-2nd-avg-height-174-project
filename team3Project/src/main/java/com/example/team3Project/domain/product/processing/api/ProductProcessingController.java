package com.example.team3Project.domain.product.processing.api;

import com.example.team3Project.domain.policy.dto.ProductNameProcessingRequest;
import com.example.team3Project.domain.policy.dto.ProductNameProcessingResponse;
import com.example.team3Project.domain.policy.entity.MarketCode;
import com.example.team3Project.domain.product.processing.application.ProductProcessingService;
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
@RequestMapping("/products/processing")
public class ProductProcessingController {

    // ProductProcessingService로 비즈니스 로직을 실행할 수 있도록 의존성 주입
    private final ProductProcessingService productProcessingService;

    @PostMapping("/name")
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
}
package com.example.team3Project.domain.product.processing.application;

import com.example.team3Project.domain.product.processing.dto.ProductProcessingRequest;
import com.example.team3Project.domain.product.processing.dto.SourcingCompletedRequest;
import com.example.team3Project.domain.product.processing.dto.SourcingProductResponse;
import org.springframework.stereotype.Component;

@Component
// 소싱의 서비스의 응답으로 받은 결과물을 가공 서비스의 요청 객체로 매핑한다.
public class SourcingProductMapper {
    public ProductProcessingRequest toProcessingRequest(SourcingCompletedRequest request) {
        return ProductProcessingRequest.of(
                request.getAsin(),
                request.getUrl(),
                request.getTitle(),
                request.getBrand(),
                request.getPrice(),
                request.getCurrency(),
                request.getUrlImage(),
                request.getImages()
        );
    }

    public ProductProcessingRequest toProcessingRequest(SourcingProductResponse response) {
        return ProductProcessingRequest.of(
                response.getAsin(),
                response.getUrl(),
                response.getTitle(),
                response.getBrand(),
                response.getPrice(),
                response.getCurrency(),
                response.getUrlImage(),
                response.getImages()
        );
    }
}

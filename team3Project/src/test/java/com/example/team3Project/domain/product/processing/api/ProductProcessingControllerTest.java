package com.example.team3Project.domain.product.processing.api;

import com.example.team3Project.domain.policy.dto.ProductNameProcessingResponse;
import com.example.team3Project.domain.policy.entity.MarketCode;
import com.example.team3Project.domain.product.processing.application.ProductProcessingService;
import com.example.team3Project.domain.product.processing.application.SourcingProductMapper;
import com.example.team3Project.domain.product.processing.dto.ProductProcessingRequest;
import com.example.team3Project.domain.product.processing.dto.ProductProcessingResultResponse;
import com.example.team3Project.domain.product.processing.dto.SourcingCompletedRequest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class ProductProcessingControllerTest {

    private MockMvc mockMvc;
    private ProductProcessingService productProcessingService;
    private SourcingProductMapper sourcingProductMapper;

    @BeforeEach
    void setUp() {
        productProcessingService = mock(ProductProcessingService.class);
        sourcingProductMapper = mock(SourcingProductMapper.class);

        ProductProcessingController controller =
                new ProductProcessingController(productProcessingService, sourcingProductMapper);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build();
    }

    @Test
    @DisplayName("소싱 ingest API는 payload를 내부 가공 요청으로 변환해 가공 서비스를 호출한다")
    void ingestSourcingProduct_mapsPayloadAndProcessesProduct() throws Exception {
        ProductProcessingRequest processingRequest = ProductProcessingRequest.of(
                "B000OV0S84",
                "/Coca-Cola-Zero-Sugar-Fridgepack-Pack/dp/B000OV0S84",
                "Sugar Soda, 12 fl oz Cans, 12 Pack - Classic Cola Soft Drink Fridge Pack",
                "Coca-Cola Zero",
                BigDecimal.valueOf(8.42),
                "USD",
                "https://m.media-amazon.com/images/I/71dPc-6s3QL._AC_UL320_.jpg",
                java.util.List.of(
                        "https://m.media-amazon.com/images/I/71dPc-6s3QL._SL1500_.jpg",
                        "https://m.media-amazon.com/images/I/81JqPbFHfUL._SL1500_.jpg"
                )
        );

        ProductProcessingResultResponse response = new ProductProcessingResultResponse(
                false,
                null,
                "가공 상품명",
                "가공 브랜드",
                BigDecimal.valueOf(8.42),
                "USD",
                BigDecimal.valueOf(1350),
                BigDecimal.valueOf(11367),
                BigDecimal.valueOf(18000),
                BigDecimal.valueOf(3000),
                "READY"
        );

        when(sourcingProductMapper.toProcessingRequest(any(SourcingCompletedRequest.class))).thenReturn(processingRequest);
        when(productProcessingService.processProduct(1L, MarketCode.COUPANG, processingRequest)).thenReturn(response);

        mockMvc.perform(
                        post("/api/sourcing/ingest")
                                .contentType("application/json")
                                .content("""
                                        {
                                          "userId": 1,
                                          "marketCode": "COUPANG",
                                          "asin": "B000OV0S84",
                                          "brand": "Coca-Cola Zero",
                                          "currency": "USD",
                                          "price": 8.42,
                                          "title": "Sugar Soda, 12 fl oz Cans, 12 Pack - Classic Cola Soft Drink Fridge Pack",
                                          "url": "/Coca-Cola-Zero-Sugar-Fridgepack-Pack/dp/B000OV0S84",
                                          "url_image": "https://m.media-amazon.com/images/I/71dPc-6s3QL._AC_UL320_.jpg",
                                          "images": [
                                            "https://m.media-amazon.com/images/I/71dPc-6s3QL._SL1500_.jpg",
                                            "https://m.media-amazon.com/images/I/81JqPbFHfUL._SL1500_.jpg"
                                          ],
                                          "variation": [
                                            {
                                              "asin": "B000OV0S84",
                                              "dimensions": {
                                                "Flavor Name": "Coca-Cola Zero Sugar",
                                                "Size": "12 fl oz (Pack of 12)"
                                              },
                                              "selected": true,
                                              "price": 8.42,
                                              "currency": "USD",
                                              "stock": "In Stock",
                                              "rating": 4.6,
                                              "reviews_count": 9360,
                                              "images": [
                                                "https://m.media-amazon.com/images/I/71dPc-6s3QL._SL1500_.jpg"
                                              ]
                                            }
                                          ]
                                        }
                                        """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.excluded").value(false))
                .andExpect(jsonPath("$.registrationStatus").value("READY"))
                .andExpect(jsonPath("$.processedProductName").value("가공 상품명"));

        ArgumentCaptor<com.example.team3Project.domain.product.processing.dto.SourcingCompletedRequest> captor =
                ArgumentCaptor.forClass(com.example.team3Project.domain.product.processing.dto.SourcingCompletedRequest.class);

        verify(sourcingProductMapper).toProcessingRequest(captor.capture());
        verify(productProcessingService).processProduct(1L, MarketCode.COUPANG, processingRequest);

        assertEquals(1L, captor.getValue().getUserId());
        assertEquals(MarketCode.COUPANG, captor.getValue().getMarketCode());
        assertEquals("B000OV0S84", captor.getValue().getAsin());
    }
}

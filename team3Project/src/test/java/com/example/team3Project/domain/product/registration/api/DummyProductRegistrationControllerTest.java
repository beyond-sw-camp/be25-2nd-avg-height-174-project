package com.example.team3Project.domain.product.registration.api;

import com.example.team3Project.domain.policy.entity.MarketCode;
import com.example.team3Project.domain.product.registration.application.ProductRegistrationService;
import com.example.team3Project.domain.product.registration.entity.DummyProductRegistration;
import com.example.team3Project.domain.product.registration.entity.RegistrationStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DummyProductRegistrationControllerTest {

    private MockMvc mockMvc;
    private ProductRegistrationService productRegistrationService;

    @BeforeEach
    void setUp() {
        // 컨트롤러 자체 동작만 검증하기 위해 스프링 전체 컨텍스트 대신 standaloneSetup을 사용한다.
        productRegistrationService = mock(ProductRegistrationService.class);
        DummyProductRegistrationController controller =
                new DummyProductRegistrationController(productRegistrationService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller).build();
    }

    @Test
    @DisplayName("등록 목록 조회 API는 userId와 marketCode에 맞는 목록을 반환한다")
    void getRegistrations_returnsRegistrationList() throws Exception {
        // given
        // 목록 조회 API는 서비스가 내려준 등록 상품 목록을 그대로 응답해야 한다.
        List<DummyProductRegistration> registrations = List.of(
                createRegistration(
                        1L,
                        "ASIN-001",
                        "첫 번째 상품",
                        RegistrationStatus.READY,
                        null,
                        BigDecimal.valueOf(20100)
                ),
                createRegistration(
                        2L,
                        "ASIN-002",
                        "두 번째 상품",
                        RegistrationStatus.BLOCKED,
                        "BLOCKED_WORD",
                        BigDecimal.ZERO
                )
        );

        when(productRegistrationService.getRegistrations(1L, MarketCode.COUPANG))
                .thenReturn(registrations);

        // when & then
        mockMvc.perform(
                        get("/products/registrations")
                                .param("userId", "1")
                                .param("marketCode", "COUPANG")
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].sourceProductId").value("ASIN-001"))
                .andExpect(jsonPath("$[0].processedProductName").value("첫 번째 상품"))
                .andExpect(jsonPath("$[0].registrationStatus").value("READY"))
                .andExpect(jsonPath("$[1].sourceProductId").value("ASIN-002"))
                .andExpect(jsonPath("$[1].registrationStatus").value("BLOCKED"))
                .andExpect(jsonPath("$[1].exclusionReason").value("BLOCKED_WORD"));
    }

    @Test
    @DisplayName("등록 단건 조회 API는 registrationId에 해당하는 상품을 반환한다")
    void getRegistration_returnsSingleRegistration() throws Exception {
        // given
        // 단건 조회 API는 PathVariable로 받은 등록 ID 기준으로 상세 정보를 반환해야 한다.
        DummyProductRegistration registration = createRegistration(
                10L,
                "ASIN-010",
                "단건 조회 상품",
                RegistrationStatus.READY,
                null,
                BigDecimal.valueOf(25300)
        );

        when(productRegistrationService.getRegistration(10L)).thenReturn(registration);

        // when & then
        mockMvc.perform(get("/products/registrations/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dummyProductRegistrationId").value(10))
                .andExpect(jsonPath("$.sourceProductId").value("ASIN-010"))
                .andExpect(jsonPath("$.processedProductName").value("단건 조회 상품"))
                .andExpect(jsonPath("$.registrationStatus").value("READY"))
                .andExpect(jsonPath("$.salePrice").value(25300));
    }

    private DummyProductRegistration createRegistration(
            Long registrationId,
            String sourceProductId,
            String processedProductName,
            RegistrationStatus registrationStatus,
            String exclusionReason,
            BigDecimal salePrice
    ) {
        // 컨트롤러 테스트에서는 서비스가 반환할 등록 엔티티를 간단히 만들어 사용한다.
        DummyProductRegistration registration = DummyProductRegistration.create(
                1L,
                MarketCode.COUPANG,
                sourceProductId,
                "https://www.amazon.com/dp/" + sourceProductId,
                processedProductName,
                "브랜드",
                BigDecimal.valueOf(10),
                "USD",
                BigDecimal.valueOf(1350),
                BigDecimal.valueOf(13500),
                salePrice,
                BigDecimal.valueOf(3000),
                registrationStatus,
                exclusionReason
        );

        // 실제 DB 저장 없이 단건 조회 JSON을 검증하기 위해 테스트에서만 ID를 주입한다.
        ReflectionTestUtils.setField(registration, "dummyProductRegistrationId", registrationId);
        return registration;
    }
}

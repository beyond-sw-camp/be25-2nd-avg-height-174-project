package com.example.team3Project.domain.product.registration.api;

import com.example.team3Project.domain.policy.entity.MarketCode;
import com.example.team3Project.domain.product.registration.application.ProductRegistrationService;
import com.example.team3Project.domain.product.registration.entity.DummyProductRegistration;
import com.example.team3Project.domain.product.registration.entity.RegistrationStatus;
import com.example.team3Project.domain.user.User;
import com.example.team3Project.global.annotation.LoginUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class DummyProductRegistrationControllerTest {

    private MockMvc mockMvc;
    private ProductRegistrationService productRegistrationService;

    @BeforeEach
    void setUp() {
        productRegistrationService = mock(ProductRegistrationService.class);
        DummyProductRegistrationController controller =
                new DummyProductRegistrationController(productRegistrationService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new LoginUserTestArgumentResolver(createUser(1L, "tester", "테스터")))
                .build();
    }

    @Test
    @DisplayName("등록 목록 조회 API는 로그인 사용자와 마켓 코드에 맞는 목록을 반환한다")
    void getRegistrations_returnsRegistrationList() throws Exception {
        List<DummyProductRegistration> registrations = List.of(
                createRegistration(1L, "ASIN-001", "첫 번째 상품", RegistrationStatus.READY, null, BigDecimal.valueOf(20100)),
                createRegistration(2L, "ASIN-002", "두 번째 상품", RegistrationStatus.BLOCKED, "BLOCKED_WORD", BigDecimal.ZERO)
        );

        when(productRegistrationService.getRegistrations(1L, MarketCode.COUPANG))
                .thenReturn(registrations);

        mockMvc.perform(
                        get("/products/registrations")
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
    @DisplayName("등록 목록 조회 API는 로그인 사용자가 없으면 401을 반환한다")
    void getRegistrations_returnsUnauthorized_whenNoLoginUser() throws Exception {
        DummyProductRegistrationController controller =
                new DummyProductRegistrationController(productRegistrationService);
        MockMvc unauthorizedMockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new LoginUserTestArgumentResolver(null))
                .build();

        unauthorizedMockMvc.perform(
                        get("/products/registrations")
                                .param("marketCode", "COUPANG")
                )
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(productRegistrationService);
    }

    @Test
    @DisplayName("등록 단건 조회 API는 로그인 사용자 소유 상품만 반환한다")
    void getRegistration_returnsSingleRegistration() throws Exception {
        DummyProductRegistration registration = createRegistration(
                10L,
                "ASIN-010",
                "단건 조회 상품",
                RegistrationStatus.READY,
                null,
                BigDecimal.valueOf(25300)
        );

        when(productRegistrationService.getRegistration(1L, 10L)).thenReturn(registration);

        mockMvc.perform(get("/products/registrations/10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.dummyProductRegistrationId").value(10))
                .andExpect(jsonPath("$.sourceProductId").value("ASIN-010"))
                .andExpect(jsonPath("$.processedProductName").value("단건 조회 상품"))
                .andExpect(jsonPath("$.registrationStatus").value("READY"))
                .andExpect(jsonPath("$.salePrice").value(25300));

        verify(productRegistrationService).getRegistration(1L, 10L);
    }

    @Test
    @DisplayName("등록 단건 조회 API는 로그인 사용자가 없으면 401을 반환한다")
    void getRegistration_returnsUnauthorized_whenNoLoginUser() throws Exception {
        DummyProductRegistrationController controller =
                new DummyProductRegistrationController(productRegistrationService);
        MockMvc unauthorizedMockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new LoginUserTestArgumentResolver(null))
                .build();

        unauthorizedMockMvc.perform(get("/products/registrations/10"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(productRegistrationService);
    }

    @Test
    @DisplayName("등록 삭제 API는 로그인 사용자 소유 상품만 삭제한다")
    void deleteRegistration_deletesOwnedRegistration() throws Exception {
        mockMvc.perform(delete("/products/registrations/10"))
                .andExpect(status().isNoContent());

        verify(productRegistrationService).deleteRegistration(1L, 10L);
    }

    @Test
    @DisplayName("등록 삭제 API는 로그인 사용자가 없으면 401을 반환한다")
    void deleteRegistration_returnsUnauthorized_whenNoLoginUser() throws Exception {
        DummyProductRegistrationController controller =
                new DummyProductRegistrationController(productRegistrationService);
        MockMvc unauthorizedMockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new LoginUserTestArgumentResolver(null))
                .build();

        unauthorizedMockMvc.perform(delete("/products/registrations/10"))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(productRegistrationService);
    }

    @Test
    @DisplayName("등록 다건 삭제 API는 선택한 상품 목록을 한 번에 삭제한다")
    void deleteRegistrations_deletesSelectedRegistrations() throws Exception {
        mockMvc.perform(
                        delete("/products/registrations")
                                .contentType("application/json")
                                .content("""
                                        {
                                          "registrationIds": [10, 11, 12]
                                        }
                                        """)
                )
                .andExpect(status().isNoContent());

        verify(productRegistrationService).deleteRegistrations(1L, List.of(10L, 11L, 12L));
    }

    @Test
    @DisplayName("등록 다건 삭제 API는 로그인 사용자가 없으면 401을 반환한다")
    void deleteRegistrations_returnsUnauthorized_whenNoLoginUser() throws Exception {
        DummyProductRegistrationController controller =
                new DummyProductRegistrationController(productRegistrationService);
        MockMvc unauthorizedMockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new LoginUserTestArgumentResolver(null))
                .build();

        unauthorizedMockMvc.perform(
                        delete("/products/registrations")
                                .contentType("application/json")
                                .content("""
                                        {
                                          "registrationIds": [10, 11]
                                        }
                                        """)
                )
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(productRegistrationService);
    }

    private DummyProductRegistration createRegistration(
            Long registrationId,
            String sourceProductId,
            String processedProductName,
            RegistrationStatus registrationStatus,
            String exclusionReason,
            BigDecimal salePrice
    ) {
        DummyProductRegistration registration = DummyProductRegistration.create(
                1L,
                MarketCode.COUPANG,
                sourceProductId,
                "https://www.amazon.com/dp/" + sourceProductId,
                "https://www.amazon.com/images/" + sourceProductId + ".jpg",
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

        ReflectionTestUtils.setField(registration, "dummyProductRegistrationId", registrationId);
        return registration;
    }

    private User createUser(Long id, String username, String nickname) {
        User user = new User();
        ReflectionTestUtils.setField(user, "id", id);
        ReflectionTestUtils.setField(user, "username", username);
        ReflectionTestUtils.setField(user, "nickname", nickname);
        return user;
    }

    private static class LoginUserTestArgumentResolver implements HandlerMethodArgumentResolver {

        private final User user;

        private LoginUserTestArgumentResolver(User user) {
            this.user = user;
        }

        @Override
        public boolean supportsParameter(MethodParameter parameter) {
            return parameter.hasParameterAnnotation(LoginUser.class)
                    && User.class.isAssignableFrom(parameter.getParameterType());
        }

        @Override
        public Object resolveArgument(MethodParameter parameter,
                                      ModelAndViewContainer mavContainer,
                                      NativeWebRequest webRequest,
                                      WebDataBinderFactory binderFactory) {
            return user;
        }
    }
}

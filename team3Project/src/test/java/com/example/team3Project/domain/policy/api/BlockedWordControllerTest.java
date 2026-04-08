package com.example.team3Project.domain.policy.api;

import com.example.team3Project.domain.policy.application.BlockedWordService;
import com.example.team3Project.domain.policy.dto.BlockedWordResponse;
import com.example.team3Project.domain.user.User;
import com.example.team3Project.global.annotation.LoginUser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.util.List;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class BlockedWordControllerTest {

    private MockMvc mockMvc;
    private BlockedWordService blockedWordService;

    @BeforeEach
    void setUp() {
        blockedWordService = mock(BlockedWordService.class);
        BlockedWordController controller = new BlockedWordController(blockedWordService);

        mockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new LoginUserTestArgumentResolver(createUser(1L, "tester", "테스터")))
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build();
    }

    @Test
    @DisplayName("소싱용 금지어 조회 API는 현재 사용자 기준 문자열 목록만 반환한다")
    void getBlockedWordsForSourcing_returnsBlockedWordStrings() throws Exception {
        when(blockedWordService.getBlockedWords(1L)).thenReturn(List.of(
                new BlockedWordResponse(1L, 1L, "금지어1"),
                new BlockedWordResponse(2L, 1L, "금지어2")
        ));

        mockMvc.perform(get("/policies/blocked-words/sourcing"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0]").value("금지어1"))
                .andExpect(jsonPath("$[1]").value("금지어2"));
    }

    @Test
    @DisplayName("소싱용 금지어 조회 API는 로그인 사용자가 없으면 401을 반환한다")
    void getBlockedWordsForSourcing_returnsUnauthorized_whenNoLoginUser() throws Exception {
        BlockedWordController controller = new BlockedWordController(blockedWordService);
        MockMvc unauthorizedMockMvc = MockMvcBuilders.standaloneSetup(controller)
                .setCustomArgumentResolvers(new LoginUserTestArgumentResolver(null))
                .setMessageConverters(new MappingJackson2HttpMessageConverter())
                .build();

        unauthorizedMockMvc.perform(get("/policies/blocked-words/sourcing"))
                .andExpect(status().isUnauthorized());
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

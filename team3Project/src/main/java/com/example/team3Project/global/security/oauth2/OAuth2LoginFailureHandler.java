package com.example.team3Project.global.security.oauth2;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Slf4j
@Component
public class OAuth2LoginFailureHandler extends SimpleUrlAuthenticationFailureHandler {

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response,
                                        AuthenticationException exception) throws IOException, ServletException {
        String errorMessage = "소셜 로그인에 실패했습니다.";

        if (exception instanceof OAuth2AuthenticationException) {
            OAuth2AuthenticationException oauth2Exception = (OAuth2AuthenticationException) exception;
            errorMessage += " (" + oauth2Exception.getError().getErrorCode() + ")";
        }

        log.error("OAuth2 로그인 실패: {}", exception.getMessage());

        // 로그인 페이지로 리다이렉트하면서 에러 메시지 전달
        request.getSession().setAttribute("oauth2Error", errorMessage);
        getRedirectStrategy().sendRedirect(request, response, "/users/login?oauth2Error=true");
    }
}

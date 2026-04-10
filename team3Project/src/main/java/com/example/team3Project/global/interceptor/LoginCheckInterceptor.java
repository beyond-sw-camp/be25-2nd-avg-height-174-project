package com.example.team3Project.global.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
public class LoginCheckInterceptor implements HandlerInterceptor {

    private static final String USER_ID_HEADER = "X-User-Id";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();

        log.info("인증 체크 인터셉터 실행 {}", requestURI);

        // 분리 프론트의 프리플라이트 요청은 인증 헤더 없이도 먼저 통과시킨다.
        if (HttpMethod.OPTIONS.matches(request.getMethod())) {
            return true;
        }

        // Gateway가 JWT 검증에 성공한 요청만 X-User-Id 헤더를 붙여서 보낸다.
        String userIdHeader = request.getHeader(USER_ID_HEADER);
        if (userIdHeader == null || userIdHeader.isBlank()) {
            log.info("인증 헤더가 없는 요청");
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "인증이 필요합니다.");
            return false;
        }

        return true;
    }
}

package com.example.team3Project.domain.sourcing.controller;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import com.example.team3Project.support.auth.RequestUserIdResolver;

import jakarta.servlet.http.HttpServletRequest;

import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/sourcing")
@RequiredArgsConstructor
public class SourcingPageController {

    @Value("${fastapi.sourcing.url}")
    private String sourcingApiUrl;

    // 소싱 페이지 접근 주소 API Gateway로 나가기 위한 주소.
    @Value("${sourcing.api-gateway-public-origin:}")
    private String apiGatewayPublicOrigin;

    private final RequestUserIdResolver requestUserIdResolver; // 유저 아이디 풀기
    private final RestTemplate restTemplate = new RestTemplate(); // 파이썬 소싱 서버에 데이터 전송하기 위한 템플릿.

    @GetMapping("/auto")
    public String autoSourcingForm(Model model) {
        model.addAttribute("sourcingApiOrigin", trimOrigin(apiGatewayPublicOrigin));
        return "sourcing-test/sourcing-form";
    }

    @PostMapping("/auto")
    @ResponseBody
    public ResponseEntity<Object> autoSourcing(
            HttpServletRequest request,
            @RequestBody Map<String, Object> body) {
        // 토큰에서 유저 ID 가져오기. 
        Long userId = requestUserIdResolver.resolveForApi(request);
        if (userId == null) { // 만약 유저 아이디가 아니라면, 에러 반환.
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "인증이 필요합니다. API Gateway에서 X-User-Id 헤더를 전달해 주세요.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
        // 토큰에서 유저아이디가 제대로 들어왔다면, 파이썬 소싱 서버에 데이터 전송.
        ResponseEntity<Object> pythonResponse = restTemplate.postForEntity(
                sourcingApiUrl,
                body,
                Object.class
        );
        // 소싱 서버에서 데이터 전송 후, 결과 반환.
        return ResponseEntity.status(pythonResponse.getStatusCode()).body(pythonResponse.getBody());
    }
    
    // API Gateway 공개 origin 설정값을 URL 조합에 쓰기 좋게 정리한다 (공백·끝 슬래시 제거).
    private static String trimOrigin(String s) {
        if (!StringUtils.hasText(s)) {
            return "";
        }
        return s.trim().replaceAll("/$", "");
    }
}

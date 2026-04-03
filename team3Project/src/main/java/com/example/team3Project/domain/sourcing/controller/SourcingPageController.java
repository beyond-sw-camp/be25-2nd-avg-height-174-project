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
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

@Controller
@RequestMapping("/sourcing")
public class SourcingPageController {

    @Value("${fastapi.sourcing.url}")
    private String sourcingApiUrl;

    /**
     * 브라우저 fetch가 게이트웨이(9000)로 나갈 때 사용. 비우면 현재 페이지 origin(상대 경로).
     */
    @Value("${sourcing.api-gateway-public-origin:}")
    private String apiGatewayPublicOrigin;

    private final RestTemplate restTemplate = new RestTemplate();

    @GetMapping("/auto")
    public String autoSourcingForm(Model model) {
        model.addAttribute("sourcingApiOrigin", trimOrigin(apiGatewayPublicOrigin));
        return "sourcing-test/sourcing-form";
    }

    // 소싱 버튼 누르면 소싱 시작.
    @PostMapping("/auto")
    @ResponseBody
    public ResponseEntity<Object> autoSourcing(
        @RequestHeader(value = "X-User-Id", required = false) String xUserId,
        @RequestBody Map<String, Object> body) {
        Long userId = parseUserId(xUserId);
        if (userId == null) {
            Map<String, Object> error = new HashMap<>();
            error.put("status", "error");
            error.put("message", "인증이 필요합니다. API Gateway로 Bearer JWT를 보내 주세요.");
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(error);
        }
        ResponseEntity<Object> pythonResponse = restTemplate.postForEntity(
                sourcingApiUrl,
                body,
                Object.class
        );

        return ResponseEntity.status(pythonResponse.getStatusCode()).body(pythonResponse.getBody());
    }

    private static String trimOrigin(String s) {
        if (!StringUtils.hasText(s)) {
            return "";
        }
        return s.trim().replaceAll("/$", "");
    }

    private static Long parseUserId(String raw) {
        if (raw == null || raw.isBlank()) {
            return null;
        }
        try {
            return Long.parseLong(raw.trim());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}

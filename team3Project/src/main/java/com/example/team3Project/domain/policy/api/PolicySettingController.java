package com.example.team3Project.domain.policy.api;

import com.example.team3Project.domain.policy.application.PolicySettingService;
import com.example.team3Project.domain.policy.dto.PolicySettingResponse;
import com.example.team3Project.domain.policy.dto.PolicySettingUpsertRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;

@RestController     // REST API 컨트롤러 -> JSON 형태로 응답 반환
@RequestMapping("policies/settings")
@RequiredArgsConstructor
public class PolicySettingController {
    // 서비스 클래스를 주입받아 비즈니스 로직을 위임한다.
    private final PolicySettingService policySettingService;

    @PutMapping // PUT 요청 처리
    public ResponseEntity<PolicySettingResponse> upsertPolicySetting(
            @RequestParam Long userId, // 사용자 식별을 위해 파라미터 받음
            // @RequestBody - Json을 자바 객체로 변환
            // @Valid - DTO 검증 적용
            // request - 클라이언트가 보낸 JSON 요청 데이터를 담고 있는 객체
            @Valid @RequestBody PolicySettingUpsertRequest request
            ) {
        // 정책 설정 여부 확인
        PolicySettingResponse response = policySettingService.upsertPolicySetting(userId, request);
        // response 객체를 HTTP 200 OK 응답으로 반환
        return ResponseEntity.ok(response);
    }
    @GetMapping ResponseEntity<PolicySettingResponse> getPolicySetting(
            @RequestParam Long userId
    ){
        PolicySettingResponse response = policySettingService.getPolicySetting(userId);
        return ResponseEntity.ok(response);
    }
}

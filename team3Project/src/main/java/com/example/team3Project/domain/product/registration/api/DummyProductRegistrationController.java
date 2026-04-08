package com.example.team3Project.domain.product.registration.api;

import com.example.team3Project.domain.policy.entity.MarketCode;
import com.example.team3Project.domain.product.registration.application.ProductRegistrationService;
import com.example.team3Project.domain.product.registration.entity.DummyProductRegistration;
import com.example.team3Project.domain.user.User;
import com.example.team3Project.global.annotation.LoginUser;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("products/registrations")
@RequiredArgsConstructor
public class DummyProductRegistrationController {

    // 등록 상품 조회 API도 현재 로그인 사용자의 데이터만 대상으로 한다.
    private final ProductRegistrationService productRegistrationService;

    @GetMapping
    public ResponseEntity<List<DummyProductRegistration>> getRegistrations(
            @LoginUser User user,
            @RequestParam MarketCode marketCode
    ) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        List<DummyProductRegistration> registrations =
                productRegistrationService.getRegistrations(user.getId(), marketCode);

        return ResponseEntity.ok(registrations);
    }

    @GetMapping("/{registrationId}")
    public ResponseEntity<DummyProductRegistration> getRegistration(
            @LoginUser User user,
            @PathVariable Long registrationId
    ) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }

        DummyProductRegistration registration =
                productRegistrationService.getRegistration(user.getId(), registrationId);

        return ResponseEntity.ok(registration);
    }
}

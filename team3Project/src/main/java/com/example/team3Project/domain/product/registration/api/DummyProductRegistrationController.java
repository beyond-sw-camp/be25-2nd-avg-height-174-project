package com.example.team3Project.domain.product.registration.api;

import com.example.team3Project.domain.policy.entity.MarketCode;
import com.example.team3Project.domain.product.processing.application.ProductProcessingService;
import com.example.team3Project.domain.product.registration.application.ProductRegistrationService;
import com.example.team3Project.domain.product.registration.entity.DummyProductRegistration;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("products/registrations")
@RequiredArgsConstructor
public class DummyProductRegistrationController {

    private final ProductRegistrationService productRegistrationService;

    // 등록 목록 전체 조회
    @GetMapping
    public ResponseEntity<List<DummyProductRegistration>> getRegistrations(
            @RequestParam Long userId,
            @RequestParam MarketCode marketCode
            ){
        List<DummyProductRegistration> registrations = productRegistrationService.getRegistrations(userId, marketCode);

        return ResponseEntity.ok(registrations);
    }

    // 등록 건별 조회
    @GetMapping("/{registrationId}")
    public ResponseEntity<DummyProductRegistration> getRegistration(
            @PathVariable Long registrationId
    ) {
        DummyProductRegistration registration =
                productRegistrationService.getRegistration(registrationId);

        return ResponseEntity.ok(registration);
    }
}

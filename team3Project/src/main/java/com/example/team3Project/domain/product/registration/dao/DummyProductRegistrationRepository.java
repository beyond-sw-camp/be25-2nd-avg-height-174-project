package com.example.team3Project.domain.product.registration.dao;

import com.example.team3Project.domain.policy.entity.MarketCode;
import com.example.team3Project.domain.product.registration.entity.DummyProductRegistration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DummyProductRegistrationRepository extends JpaRepository<DummyProductRegistration, Long> {
    List<DummyProductRegistration> findByUserIdAndMarketCode(Long userId, MarketCode marketCode);
}

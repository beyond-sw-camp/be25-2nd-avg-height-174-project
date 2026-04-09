package com.example.team3Project.domain.product.registration.dao;

import com.example.team3Project.domain.policy.entity.MarketCode;
import com.example.team3Project.domain.product.registration.entity.DummyProductRegistration;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DummyProductRegistrationRepository extends JpaRepository<DummyProductRegistration, Long> {
    List<DummyProductRegistration> findByUserIdAndMarketCode(Long userId, MarketCode marketCode);

    // 단건 조회 시 다른 사용자의 등록 상품이 섞이지 않도록 userId까지 함께 조건으로 건다.
    Optional<DummyProductRegistration> findByDummyProductRegistrationIdAndUserId(Long registrationId, Long userId);

    // 선택 삭제에서는 현재 로그인 사용자의 소유 데이터만 한 번에 조회한다.
    List<DummyProductRegistration> findAllByDummyProductRegistrationIdInAndUserId(List<Long> registrationIds, Long userId);

    // 같은 사용자/마켓/원본 상품 조합은 한 건만 유지하고 재처리 시 갱신한다.
    Optional<DummyProductRegistration> findByUserIdAndMarketCodeAndSourceProductId(
            Long userId,
            MarketCode marketCode,
            String sourceProductId
    );
}

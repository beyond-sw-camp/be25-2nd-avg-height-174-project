package com.example.team3Project.domain.product.coupang.dao;

import com.example.team3Project.domain.product.coupang.entity.DummyCoupangProduct;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DummyCoupangProductRepository extends JpaRepository<DummyCoupangProduct, Long> {

    // 현재 로그인 사용자의 쿠팡 더미 상품 목록만 최신순으로 조회한다.
    List<DummyCoupangProduct> findByUserIdOrderByDummyCoupangProductIdDesc(Long userId);

    // 상세 조회는 다른 사용자의 상품이 섞이지 않도록 userId까지 함께 건다.
    Optional<DummyCoupangProduct> findByDummyCoupangProductIdAndUserId(Long dummyCoupangProductId, Long userId);

    // 같은 사용자의 같은 원본 상품은 쿠팡 더미 상품 1건만 유지한다.
    Optional<DummyCoupangProduct> findByUserIdAndSourceProductId(Long userId, String sourceProductId);
}

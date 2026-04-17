package com.example.team3Project.domain.order.dao;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByAutoOrderStatus(String status);
    List<Order> findByUserId(Long userId);

    // 사용자가 등록한 상품의 월별 수익 집계
    @Query("""
            SELECT YEAR(o.createdAt), MONTH(o.createdAt),
                   SUM(o.quantity), SUM(o.totalAmount), SUM(o.margin)
            FROM Order o
            WHERE o.dummyCoupangProduct IS NOT NULL
              AND o.dummyCoupangProduct.userId = :userId
              AND o.status <> 'CANCELLED'
              AND o.createdAt IS NOT NULL
            GROUP BY YEAR(o.createdAt), MONTH(o.createdAt)
            ORDER BY YEAR(o.createdAt) ASC, MONTH(o.createdAt) ASC
            """)
    List<Object[]> findMonthlyRevenueByUserId(@Param("userId") Long userId);
}

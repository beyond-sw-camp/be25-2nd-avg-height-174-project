package com.example.team3Project.domain.shipment.dao;

import com.example.team3Project.domain.shipment.enums.Shipment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ShipmentRepository extends JpaRepository<Shipment, Long> {
    Optional<Shipment> findByOrderId(Long orderId);

    boolean existsByOrderId(Long orderId);

    boolean existsByTrackingNumber(String trackingNumber);
}

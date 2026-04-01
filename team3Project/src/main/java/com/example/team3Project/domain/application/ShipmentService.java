package com.example.team3Project.domain.application;

import com.example.team3Project.domain.dao.ShipmentRepository;
import com.example.team3Project.domain.dto.ShipmentCreateRequest;
import com.example.team3Project.domain.dto.ShipmentResponse;
import com.example.team3Project.domain.dto.ShipmentStatusUpdateRequest;
import com.example.team3Project.domain.enums.Shipment;
import com.example.team3Project.domain.enums.ShipmentStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ShipmentService {

    private final ShipmentRepository shipmentRepository;

    public ShipmentResponse createShipment(ShipmentCreateRequest request) {
        validateCreateRequest(request);

        if (shipmentRepository.existsByOrderId(request.getOrderId())) {
            throw new IllegalStateException("이미 해당 주문의 배송 정보가 존재합니다.");
        }

        if (shipmentRepository.existsByTrackingNumber(request.getTrackingNumber())) {
            throw new IllegalStateException("이미 등록된 운송장 번호입니다.");
        }

        Shipment shipment = Shipment.builder()
                .orderId(request.getOrderId())
                .trackingNumber(request.getTrackingNumber())
                .courier(request.getCourier())
                .status(ShipmentStatus.READY)
                .build();

        Shipment savedShipment = shipmentRepository.save(shipment);
        return ShipmentResponse.from(savedShipment);
    }

    @Transactional(readOnly = true)
    public ShipmentResponse getShipmentByOrderId(Long orderId) {
        Shipment shipment = shipmentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new IllegalArgumentException("해당 주문의 배송 정보가 존재하지 않습니다."));

        return ShipmentResponse.from(shipment);
    }

    public ShipmentResponse updateShipmentStatus(Long shipmentId, ShipmentStatusUpdateRequest request) {
        if (request.getStatus() == null || request.getStatus().isBlank()) {
            throw new IllegalArgumentException("status는 필수입니다.");
        }

        Shipment shipment = shipmentRepository.findById(shipmentId)
                .orElseThrow(() -> new IllegalArgumentException("해당 배송 정보가 존재하지 않습니다."));

        ShipmentStatus status;
        try {
            status = ShipmentStatus.valueOf(request.getStatus().toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("status는 READY, SHIPPING, DELIVERED 중 하나여야 합니다.");
        }

        shipment.updateStatus(status);
        return ShipmentResponse.from(shipment);
    }

    private void validateCreateRequest(ShipmentCreateRequest request) {
        if (request.getOrderId() == null) {
            throw new IllegalArgumentException("orderId는 필수입니다.");
        }
        if (request.getTrackingNumber() == null || request.getTrackingNumber().isBlank()) {
            throw new IllegalArgumentException("trackingNumber는 필수입니다.");
        }
        if (request.getCourier() == null || request.getCourier().isBlank()) {
            throw new IllegalArgumentException("courier는 필수입니다.");
        }
    }

}

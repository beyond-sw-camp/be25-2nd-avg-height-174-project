package com.example.team3Project.domain.settlement.application;


import com.example.team3Project.domain.settlement.dao.Card;
import com.example.team3Project.domain.settlement.dao.CardRepository;
import com.example.team3Project.domain.order.dao.Order;
import com.example.team3Project.domain.order.dao.OrderRepository;
import com.example.team3Project.domain.settlement.dao.Payment;
import com.example.team3Project.domain.settlement.dao.PaymentRepository;
import com.example.team3Project.domain.settlement.dto.DecryptedCardInfo;
import com.example.team3Project.domain.settlement.dto.PaymentRequest;
import com.example.team3Project.domain.settlement.enums.PaymentStatus;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.YearMonth;

@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final CardRepository cardRepository;
    private final OrderRepository orderRepository;
    private final CardService cardService;

    // 결제
    @Transactional
    public Payment processPayment(Long orderId, Long cardId) {

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("주문 없음"));

        Card card = cardRepository.findById(cardId)
                .orElseThrow(() -> new RuntimeException("카드 없음"));

        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(order.getTotalAmount());
        payment.setCard(card);

        // 카드 활성화
        if (!card.isActive()) {
            return failPayment(order, payment, PaymentStatus.FAILED_CARD_INACTIVE);
        }

        // 카드 정보 복호화
        DecryptedCardInfo cardInfo = cardService.getDecryptedCard(cardId);

        // 카드 유효기간 확인
        if (isCardExpired(cardInfo.getExpiry())) {
            return failPayment(order, payment, PaymentStatus.FAILED_CARD_EXPIRED);
        }

        // 카드 한도
        if (order.getTotalAmount() > card.getCardLimit()) {
            return failPayment(order, payment, PaymentStatus.FAILED_LIMIT_EXCEEDED);
        }

        // 금액
        if (order.getTotalAmount() > card.getBalance()) {
            return failPayment(order, payment, PaymentStatus.FAILED_INSUFFICIENT_BALANCE);
        }

        card.setBalance(card.getBalance() - order.getTotalAmount());

        payment.setStatus("SUCCESS");
        order.setStatus("PAID");
        order.setAutoOrderStatus("Ordered");

        return paymentRepository.save(payment);
    }

    private boolean isCardExpired(String expiry) {

        // expiry format : MM/YY

        String[] parts = expiry.split("/");

        int month = Integer.parseInt(parts[0]);
        int year = 2000 + Integer.parseInt(parts[1]);

        YearMonth cardDate = YearMonth.of(year, month);
        YearMonth now = YearMonth.now();

        return cardDate.isBefore(now);
    }

    // 결제 실패
    private Payment failPayment(Order order, Payment payment, PaymentStatus status) {
        payment.setStatus(status.name());
        order.setStatus("FAILED");
        order.setAutoOrderStatus("FAILED");
        return paymentRepository.save(payment);
    }

    // 환불
    @Transactional
    public Payment refundPayment(Long paymentId) {

        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("결제 없음"));

        if (!"SUCCESS".equals(payment.getStatus())) {
            throw new RuntimeException("환불 불가능한 결제");
        }

        Card card = payment.getCard();
        Order order = payment.getOrder();

        // 카드 잔액 복구
        card.setBalance(card.getBalance() + payment.getAmount());

        // 환불
        payment.setStatus("REFUNDED");

        // 결제 취소
        order.setStatus("CANCELED");
        order.setAutoOrderStatus("FAILED");

        return paymentRepository.save(payment);
    }
}

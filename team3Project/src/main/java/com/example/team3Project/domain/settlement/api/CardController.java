package com.example.team3Project.domain.settlement.api;


import com.example.team3Project.domain.settlement.dao.Card;
import com.example.team3Project.domain.settlement.dao.CardRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cards")
public class CardController {
    private final CardRepository cardRepository;

    // 카드 등록
    @PostMapping
    public Card createCard(@RequestBody Card card) {
        return cardRepository.save(card);
    }

    // 카드 전체 조회
    @GetMapping
    public List<Card> getCards() {
        return cardRepository.findAll();
    }

    // 카드 단건 조회
    @GetMapping("/{id}")
    public Card getCard(@PathVariable Long id) {
        return cardRepository.findById(id).orElseThrow();
    }

    // 카드 활성 / 비활성
    @PatchMapping("/{id}/toggle")
    public Card toggleCard(@PathVariable Long id) {
        Card card = cardRepository.findById(id).orElseThrow();
        card.setActive(!card.isActive());
        return cardRepository.save(card);
    }

    // 카드 삭제
    @DeleteMapping("/{id}")
    public void deleteCard(@PathVariable Long id) {
        cardRepository.deleteById(id);
    }

    // 카드 수정
    @PutMapping("/{id}")
    public Card updateCard(@PathVariable Long id, @RequestBody Card updatedCard) { // 단순 CURD라 Entity를 직접 사용함

        Card card = cardRepository.findById(id).orElseThrow(); // 기존 카드 조회

        card.setCardType(updatedCard.getCardType());
        card.setCardNumber(updatedCard.getCardNumber());
        card.setBalance(updatedCard.getBalance());
        card.setCardLimit(updatedCard.getCardLimit());
        card.setActive(updatedCard.isActive());

        return cardRepository.save(card);
    }
}

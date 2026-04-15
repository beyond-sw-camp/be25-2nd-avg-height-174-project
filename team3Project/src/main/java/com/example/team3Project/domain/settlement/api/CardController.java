package com.example.team3Project.domain.settlement.api;


import com.example.team3Project.domain.settlement.application.CardService;
import com.example.team3Project.domain.settlement.dao.CardRepository;
import com.example.team3Project.domain.settlement.dto.CardRequest;
import com.example.team3Project.domain.settlement.dto.CardResponse;
import com.example.team3Project.domain.settlement.dto.DecryptedCardInfo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/cards")
public class CardController {
    private final CardService cardService;
    private final CardRepository cardRepository;

    // 카드 등록
    @PostMapping
    public CardResponse createCard(@RequestBody CardRequest request) {
        return cardService.createCard(request);
    }

    // 카드 전체 조회
    @GetMapping
    public List<CardResponse> getCards() {
        return cardService.getCards();
    }

    // 카드 단건 조회
    @GetMapping("/{id}")
    public CardResponse getCard(@PathVariable Long id) {
        return cardService.getCard(id);
    }

    // 암호 해독 조회
    @GetMapping("/{id}/decrypt")
    public DecryptedCardInfo decryptCard(@PathVariable Long id) {
        return cardService.getDecryptedCard(id);
    }

    // 활성/비활성화 토글
    @PatchMapping("/{id}/toggle")
    public CardResponse toggleCard(@PathVariable Long id) {
        return cardService.toggleCard(id);
    }

    // 카드 삭제
    @DeleteMapping("/{id}")
    public void deleteCard(@PathVariable Long id) {
        cardService.deleteCard(id);
    }

}

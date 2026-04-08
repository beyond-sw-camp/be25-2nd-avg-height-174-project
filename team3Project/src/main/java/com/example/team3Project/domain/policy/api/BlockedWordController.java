package com.example.team3Project.domain.policy.api;

import com.example.team3Project.domain.policy.application.BlockedWordService;
import com.example.team3Project.domain.policy.dto.BlockedWordCreateRequest;
import com.example.team3Project.domain.policy.dto.BlockedWordResponse;
import com.example.team3Project.domain.policy.dto.BlockedWordUpdateRequest;
import com.example.team3Project.domain.user.User;
import com.example.team3Project.global.annotation.LoginUser;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/policies/blocked-words")
public class BlockedWordController {

    private final BlockedWordService blockedWordService;

    @PostMapping("/post-test/{id}")
    public int postTest(@PathVariable int id) {
        return id;
    }

    // POST 요청을 처리한다. - 금지어 등록
    @PostMapping
    public ResponseEntity<BlockedWordResponse> createBlockedWord(
            @RequestParam Long userId,
            @Valid @RequestBody BlockedWordCreateRequest request) {
        BlockedWordResponse response = blockedWordService.createBlockedWord(userId, request);
        return ResponseEntity.ok(response);
    }

    // GET 요청을 처리한다. - 사용자의 금지어 목록 전체 조회
    @GetMapping
    public ResponseEntity<List<BlockedWordResponse>> getBlockedWords(
            @RequestParam Long userId
    ) {
        List<BlockedWordResponse> response = blockedWordService.getBlockedWords(userId);
        return ResponseEntity.ok(response);
    }

    // sourcing의 GET 요청 처리 - 사용자 기준 금지어 문자열 리스트 제공
    @GetMapping("/sourcing")
    public ResponseEntity<List<String>> getBlockedWordsForSourcing(@LoginUser User user) {
        if (user == null) {
            return ResponseEntity.status(401).build();
        }
        List<String> response = blockedWordService.getBlockedWords(user.getId())
                .stream()
                .map(BlockedWordResponse::getBlockedWord)
                .toList();

        return ResponseEntity.ok(response);
    }

    // DELETE 요청을 처리한다. - 금지어 삭제
    @DeleteMapping("/{userBlockedWordId}")
    public ResponseEntity<Void> deleteBlockedWord(
            @PathVariable Long userBlockedWordId,
            @RequestParam Long userId
    ) {
        blockedWordService.deleteBlockedWord(userId, userBlockedWordId);
        return ResponseEntity.noContent().build();
    }

    // PUT 요청을 처리한다. - 금지어 수정
    @PutMapping("/{userBlockedWordId}")
    public ResponseEntity<BlockedWordResponse> updateBlockedWord(
            @PathVariable Long userBlockedWordId,
            @RequestParam Long userId,
            @Valid @RequestBody BlockedWordUpdateRequest request
    ) {
        BlockedWordResponse response = blockedWordService.updateBlockedWord(userId, userBlockedWordId, request);
        return ResponseEntity.ok(response);
    }

    /*
        ResponseEntity
          - HTTP 응답 전체를 직접 제어하기 위한 객체
          - 응답 본문(body) + 헤더(headers) + 상태코드(status code)를 함께 담는 객체
          - HttpEntity + HTTP status Code
          - 상태코드, 응답 본문, 헤더를 직접 작성하여 응답을 더 명확하게 통제하고 싶을 때 사용한다.
          - ex) ResponseEntity.ok(response);
              - 응답 상태 코드: 200(ok)
              - 응답 상태 본문 : response 객체
          - ex2) ResponseEntity.status(HttpStatus.CREATED).body(response);
              - 상태 직접 지정
              - 응답 상태 코드: 201(created)
              - 응답 상태 본문 : response 객체
    */

    /*
        @RestController
        - @Controller + @ResponseBody
        - @RequestMapping 계열은 기본적으로 반환값을 HTTP 응답 본문에 써야 한다.
            - @Controller : 웹 컨트롤러
                - 보통 @RequestMapping과 함께 사용한다고 설명하지만 반환값이 자동으로 JSON이 되지는 않는다.
            - @RestController : 메서드마다 @ResponseBody를 따로 붙이지 않아도 JSON 같은 응답 본문으로 직렬화된다.
                - 무조건 JSON만 반환하지는 않는다.
                - JSON이 자주 나오는 건 @RestController 때문만이 아니라 Spring이 HTTP 메시지 컨버터(HttpMessageConverter) 로 반환 객체를 직렬화하기 때문이다.
    */
}

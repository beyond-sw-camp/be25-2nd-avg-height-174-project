package com.example.team3Project.domain.policy.api;

import com.example.team3Project.domain.policy.application.BlockedWordService;
import com.example.team3Project.domain.policy.dto.BlockedWordCreateRequest;
import com.example.team3Project.domain.policy.dto.BlockedWordResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController // JSON 응답을 반환한다. REST API용 컨트롤러
@RequiredArgsConstructor
@RequestMapping("/policies/blocked-words")      // 컨트롤러의 기본 URL 경로
public class BlockedWordController {
    // 서비스 클래스 주입 - 비즈니스 로직은 해당 서비스 클래스에서 진행한다.
    private final BlockedWordService blockedWordService;

    // Post 요청을 처리한다. - 금지어 등록
    @PostMapping
    public ResponseEntity<BlockedWordResponse> createBlockedWord(
            @RequestParam Long userId,  // 아직 인증 기능 X -> 어떤 사용자의 금지어인지 URL 파라미터로 받는다.
            // @RequestBody - RequestBody의 JSON을 DTO로 받는다.
            // @Valid - DTO 검증 (여기서는 금지어 문자열이 @NOTNULL인지 확인한다.)
            @Valid @RequestBody BlockedWordCreateRequest request) {
        // 서비스 클래스에게 금지어 등록을 맡긴 후 저장한 엔터티의 응답 DTO를 받는다.
        BlockedWordResponse response = blockedWordService.createBlockedWord(userId, request);
        // 응답 DTO를 HTTP OK 응답으로 클라이언트에게 돌려준다.
        return ResponseEntity.ok(response);
    }

    // GET 요청을 처리한다. - 사용자의 금지어 목록 전체 조회
    @GetMapping
    public ResponseEntity<List<BlockedWordResponse>> getBlockedWords(
            @RequestParam Long userId // 아직 인증 기능 X -> 어떤 사용자의 금지어인지 URL 파라미터로 받는다.
    ) {
        // 서비스에게 특정 사용자의 금지어 목록 조회를 맡긴 후 조회된 목록을 받아온다.
        List<BlockedWordResponse> response = blockedWordService.getBlockedWords(userId);
        // 조회된 목록을 200 OK 응답으로 돌려준다.
        return ResponseEntity.ok(response);
    }


    // DELETE 요청을 처리한다. - 금지어 삭제
    @DeleteMapping("/{userBlockedWordId}")
    public ResponseEntity<Void> deleteBlockedWord(
            @PathVariable Long userBlockedWordId,  // URL 경로에 들어있는 금지어 ID를 받는다.
            @RequestParam Long userId    // 웹 요청 파라미터를 메서드 파라미터에 바인딩한다. - 어느 사용자의 요청인지 쿼리 파라미터로 받음
    ) {
        // 서비스에서 삭제 로직을 돌린다.
        blockedWordService.deleteBlockedWord(userId, userBlockedWordId);
        return ResponseEntity.noContent().build();
        // Http 상태 코드 - 204(noContent)
        // 응답 body는 따로 보내지 않는다.
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

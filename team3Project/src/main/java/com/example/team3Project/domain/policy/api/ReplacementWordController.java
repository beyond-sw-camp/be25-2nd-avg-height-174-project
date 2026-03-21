package com.example.team3Project.domain.policy.api;

import com.example.team3Project.domain.policy.application.ReplacementWordService;
import com.example.team3Project.domain.policy.dto.ReplacementWordCreateRequest;
import com.example.team3Project.domain.policy.dto.ReplacementWordResponse;
import com.example.team3Project.domain.policy.dto.ReplacementWordUpdateRequest;
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
@RequestMapping("/policies/replacement-words")
public class ReplacementWordController {

    private final ReplacementWordService replacementWordService;

    // 치환어 등록
    @PostMapping
    public ResponseEntity<ReplacementWordResponse> createReplacementWord(
            @RequestParam Long userId,
            // ReplacementWordCreateRequest : 등록 요청 JSON을 받는 DTO
            @Valid @RequestBody ReplacementWordCreateRequest request
    ){
        // ReplacementWordResponse : 등록 결과와 조회 결과를 응답으로 내려주는 DTO
        // replacementWordService의 치환어 등록 메서드를 호출하여 해당 로직을 실행한다.
        ReplacementWordResponse response = replacementWordService.createReplacementWord(userId, request);
        return ResponseEntity.ok(response);
    }

    // 치환어 조회
    @GetMapping
    public ResponseEntity<Object> getReplacementWords(@RequestParam Long userId){
        // replacementWordService의 치환어 조회 메서드를 호출하여 해당 로직을 실행한다.
        List<ReplacementWordResponse> response = replacementWordService.getReplacementWords(userId);
        return ResponseEntity.ok(response);
    }

    // 치환어 삭제
    // @RequestParam : URL 쿼리 파라미터
    // @PathVariable : URL 경로 자체에 들어 있는 값 - URL 경로 중 일부를 변수처럼 사용, 특정 리소스의 ID를 받을 때 사용
    // {userReplacementWordId}에 PathVariable인 userReplacementWordId에 대입할 값이 들어온다.
    @DeleteMapping("/{userReplacementWordId}")
    public ResponseEntity<Void> deleteReplacementWord(@PathVariable Long userReplacementWordId, @RequestParam Long userId){
        // replacementWordService의 치환어 삭제 메서드를 호출하여 해당 로직을 실행한다.
        replacementWordService.deleteReplacementWord(userId, userReplacementWordId);
        return ResponseEntity.noContent().build();
    }

    // 치환어 수정
    @PutMapping("/{userReplacementWordId}")
    public ResponseEntity<ReplacementWordResponse> updateReplacementWord(
            @PathVariable Long userReplacementWordId,
            @RequestParam Long userId,
            @Valid @RequestBody ReplacementWordUpdateRequest request
    ){
        // replacementWordService의 치환어 수정 메서드를 호출하여 해당 로직을 실행한다.
        ReplacementWordResponse response = replacementWordService.updateReplacementWord(userId, userReplacementWordId, request);
        return ResponseEntity.ok(response);
    }
}

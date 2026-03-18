package com.example.team3Project.domain.policy.application;

import com.example.team3Project.domain.policy.dao.UserBlockedWordRepository;
import com.example.team3Project.domain.policy.dto.BlockedWordCreateRequest;
import com.example.team3Project.domain.policy.dto.BlockedWordResponse;
import com.example.team3Project.domain.policy.entity.UserBlockedWord;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional  // DB 작업을 하나의 트랜잭션 안에서 처리하도록 한다. 쓰기, 조회 작업
public class BlockedWordService {
    // 해당 Repository를 통해 DB에 저장, 조회한다.
    private final UserBlockedWordRepository userBlockedWordRepository;

    // 금지어 1건을 등록하는 메서드 (2가지 기능)
    // - DB에 금지어를 저장
    // - 저장 결과를 응답 형태로 돌려줌 : 보통 등록 직후 화면에서 바로 보여주거나 응답으로 확인하는 경우가 많음
    public BlockedWordResponse createBlockedWord(Long userId, BlockedWordCreateRequest request) {
        // 새로운 엔터티 인스턴스를 만들어서 담아 놓음
        UserBlockedWord blockedWord = UserBlockedWord.create(userId, request.getBlockedWord());
        // 담아 놓은 엔터티 인스턴스를 DB에 저장
        UserBlockedWord saved = userBlockedWordRepository.save(blockedWord);
        /*
            CrudRepository.save(entity)
            - 주어진 엔터티를 저장한다.
            - 새 엔터티는 저장, 기존 엔터티는 병합(업데이트)한다.
            - @Version 필드 등을 보고 기존 엔터티인지 확인한다.
        */

        // 응답 DTO로 바꿔서 반환한다. - 해당 결과를 클라이언트에게 보낸다.
        return new BlockedWordResponse(
                saved.getUserBlockedWordId(),
                saved.getUserId(),
                saved.getBlockedWord()
        );
    }

    // 특정 사용자의 금지어 목록 전체를 조회하는 메서드
    @Transactional(readOnly = true)   // 읽기 전용 트랜잭션
    public List<BlockedWordResponse> getBlockedWords(Long userId){
        // 특정 사용자의 금지어 목록 가져오기 - List 반환형 : 금지어가 여러 개일 수 있음
        /*
            findAllByUserId()
            - 파생 쿼리 메서드 : 메서드 이름으로부터 쿼리를 유도한다.
            - find...By : 조회 메서드
            - UserId : 엔터티의 필드명 userId 기준 조회
            - findby + 필드명 패턴을 지원한다.
        */
        return userBlockedWordRepository.findAllByUserId(userId)
                .stream()
                .map(blockedWord ->new BlockedWordResponse(
                        blockedWord.getUserBlockedWordId(),
                        blockedWord.getUserId(),
                        blockedWord.getBlockedWord()
                )).toList();
    }
}

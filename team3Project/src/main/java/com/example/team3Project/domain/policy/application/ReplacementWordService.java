package com.example.team3Project.domain.policy.application;

import com.example.team3Project.domain.policy.dao.UserReplacementWordRepository;
import com.example.team3Project.domain.policy.dto.ReplacementWordCreateRequest;
import com.example.team3Project.domain.policy.dto.ReplacementWordResponse;
import com.example.team3Project.domain.policy.entity.UserReplacementWord;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ReplacementWordService {
    private final UserReplacementWordRepository userReplacementWordRepository;

    // 치환어 1건을 등록하는 메서드
    public ReplacementWordResponse createReplacementWord(Long userId, ReplacementWordCreateRequest request) {
        // (사용자 ID, 원본단어) 쌍으로 조회하여 이미 해당 원본단어에 대해 치환어가 있는지 확인
        /*userReplacementWordRepository.findByUserIdAndSourceWord(userId, request.getSourceWord())
                .ifPresent(replacementWord -> {
                    throw new ReplacementWordAlreadyExistsException("이미 등록된 치환어입니다.");
                });*/

        // 해당 치환 정책이 존재하지 않으면 요청으로 받은 객체를 담은 엔터티 인스턴스 생성
        UserReplacementWord userReplacementWord = UserReplacementWord.create(
                userId,
                request.getSourceWord(),
                request.getReplacementWord()
        );

        // 해당 인스턴스 객체를 DB에 저장함
        UserReplacementWord saved = userReplacementWordRepository.save(userReplacementWord);

        return new ReplacementWordResponse(
                saved.getUserReplacementWordId(),
                saved.getUserId(),
                saved.getSourceWord(),
                saved.getReplacementWord()
        );
    }

    @Transactional(readOnly = true)
    public List<ReplacementWordResponse> getReplacementWords(Long userId){
        return UserReplacementWordRepository.findAllByUserId(userId)
                .stream()
                .map(replacementWord -> new ReplacementWordResponse(
                        replacementWord.getUserReplacementWordId(),
                        replacementWord.getUserId(),
                        replacementWord.getSourceWord(),
                        replacementWord.getReplacementWord()
                ))
                .toList();
    }
}


package com.example.team3Project.domain.policy.application;

import com.example.team3Project.domain.policy.dto.BlockedWordResponse;
import com.example.team3Project.domain.policy.dto.PolicyBundle;
import com.example.team3Project.domain.policy.dto.PolicySettingResponse;
import com.example.team3Project.domain.policy.dto.ReplacementWordResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service  // 서비스 계층
@RequiredArgsConstructor  // 생성자 주입
@Transactional(readOnly = true)  // 조회 메서드를 읽기 전용 트랜잭션으로 처리한다.
@Slf4j
public class PolicyQueryService {
    private final PolicySettingService policySettingService;   // 정책 설정 조회용 서비스
    private final BlockedWordService blockedWordService;    // 금지어 조회용 서비스
    private final ReplacementWordService replacementWordService;    // 치환어 조회용 서비스

    // 같은 userId로 정책을 가져올 때에 이전에 가져온 결과를 캐시에서 재사용할 수 있도록 한다.
    // @Cacheable : 메서드 호출 결과를 캐시한다.
    @Cacheable(value = "policyBundle", key = "#userId")
    public PolicyBundle getPolicyBundle(Long userId){
        // getPolicyBundle 메서드가 실제로 실행될 때 로그가 찍힘
        // 정상적으로 작동 : 첫 번째에만 로그가 찍혀야 함 - 캐시로 불러옴
        log.info("PolicyBundle 조회 실행 - userId = {}", userId);
        // 캐시 미스일 때 실행됨
        PolicySettingResponse policySetting = policySettingService.getPolicySetting(userId); // 정책 설정 가져오기
        List<BlockedWordResponse> blockedWords = blockedWordService.getBlockedWords(userId); // 사용자의 금지어 목록 가져오기
        List<ReplacementWordResponse> replacementWords = replacementWordService.getReplacementWords(userId); // 사용자의 치환어 목록 가져오기

        // 반환값 전체가 캐시에 저장됨
        return new PolicyBundle(
          policySetting,
          blockedWords,
          replacementWords
        );
    }
}

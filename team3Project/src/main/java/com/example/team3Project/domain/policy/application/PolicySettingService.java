package com.example.team3Project.domain.policy.application;

import com.example.team3Project.domain.policy.dao.UserPolicySettingRepository;
import com.example.team3Project.domain.policy.dto.PolicySettingResponse;
import com.example.team3Project.domain.policy.dto.PolicySettingUpsertRequest;
import com.example.team3Project.domain.policy.entity.MarketCode;
import com.example.team3Project.domain.policy.entity.UserPolicySetting;
import com.example.team3Project.domain.policy.exception.PolicySettingNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service        // 서비스 계층 컴포넌트로 등록
@Transactional  // 트랜잭션 경계를 선언하는 데 사용 - 이 클래스의 메서드가 DB 작업을 하나의 트랜잭션 안에서 처리하도록 한다.
@RequiredArgsConstructor
// controller와 repository 사이에서 실제 비즈니스 로직 구현
public class PolicySettingService {
    // 해당 Repository를 통해서 DB 접근
    private final UserPolicySettingRepository userPolicySettingRepository;

    // @CacheEvict(value = "policyBundle", key = "#userId")    // policyBundle 캐시에서 현재 사용자 userId에 해당하는 캐시 엔트리만 현재 메서드가 성공한 뒤 제거한다.

    // 정책 설정 데이터를 DB에 저장, 수정하는 메서드
    public PolicySettingResponse upsertPolicySetting(Long userId, MarketCode marketCode, PolicySettingUpsertRequest request){
        // JPA가 관리하는 entity 클래스
        UserPolicySetting setting = userPolicySettingRepository.findByUserIdAndMarketCode(userId, marketCode)
                // Optional의 map메서드 - findByUserIdAndMarketCode 메서드로 값이 들어있는지 확인 후 존재하면 가공하여 다시 UserPolicySetting에 담는다.
                .map(existing -> {
                    existing.update(
                            request.getTargetMarginRate(),
                            request.getMinMarginAmount(),
                            request.getMarketFeeRate(),
                            request.getCardFeeRate(),
                            request.getExchangeRate(),
                            request.getRoundingUnit(),
                            request.isAmazonAutoPricingEnabled(),
                            request.isCompetitorAutoPricingEnabled(),
                            request.isMinMarginProtectEnabled(),
                            request.isPriceAutoUpdateEnabled(),
                            request.isStopLossEnabled(),
                            request.getShippingFeeType(),
                            request.getBaseShippingFee(),
                            request.getRemoteAreaExtraShippingFee(),
                            request.getReturnShippingFee()
                    );
                    return existing;
                })
                // Optional의 orElseGet 메서드 UserPolicySetting이 비어있을 때만 실행되어 대체 값을 공급해주느 메서드
                .orElseGet(() -> UserPolicySetting.create(
                        userId,
                        marketCode,
                        request.getTargetMarginRate(),
                        request.getMinMarginAmount(),
                        request.getMarketFeeRate(),
                        request.getCardFeeRate(),
                        request.getExchangeRate(),
                        request.getRoundingUnit(),
                        request.isAmazonAutoPricingEnabled(),
                        request.isCompetitorAutoPricingEnabled(),
                        request.isMinMarginProtectEnabled(),
                        request.isPriceAutoUpdateEnabled(),
                        request.isStopLossEnabled(),
                        request.getShippingFeeType(),
                        request.getBaseShippingFee(),
                        request.getRemoteAreaExtraShippingFee(),
                        request.getReturnShippingFee()
                ));
        UserPolicySetting saved = userPolicySettingRepository.save(setting);

        return new PolicySettingResponse(
                saved.getUserPolicySettingId(),
                saved.getUserId(),
                saved.getMarketCode(),
                saved.getTargetMarginRate(),
                saved.getMinMarginAmount(),
                saved.getMarketFeeRate(),
                saved.getCardFeeRate(),
                saved.getExchangeRate(),
                saved.getRoundingUnit(),
                saved.isAmazonAutoPricingEnabled(),
                saved.isCompetitorAutoPricingEnabled(),
                saved.isMinMarginProtectEnabled(),
                saved.isPriceAutoUpdateEnabled(),
                saved.isStopLossEnabled(),
                saved.getShippingFeeType(),
                saved.getBaseShippingFee(),
                saved.getRemoteAreaExtraShippingFee(),
                saved.getReturnShippingFee()
        );
    }

    // 정책 설정 데이터를 DB에서 조회하는 메서드
    @Transactional(readOnly = true) // 조회 전용 트랜잭션 - 읽기 전용임을 보여줌
    public PolicySettingResponse getPolicySetting(Long userId, MarketCode marketCode){
        UserPolicySetting setting = userPolicySettingRepository.findByUserIdAndMarketCode(userId, marketCode)
                // id로 정책 설정 조회시 실패하면 예외 발생
                .orElseThrow(() -> new PolicySettingNotFoundException("정책 설정이 존재하지 않습니다."));
        // 있으면 response DTO로 변환 후 반환
        return new PolicySettingResponse(
                setting.getUserPolicySettingId(),
                setting.getUserId(),
                setting.getMarketCode(),
                setting.getTargetMarginRate(),
                setting.getMinMarginAmount(),
                setting.getMarketFeeRate(),
                setting.getCardFeeRate(),
                setting.getExchangeRate(),
                setting.getRoundingUnit(),
                setting.isAmazonAutoPricingEnabled(),
                setting.isCompetitorAutoPricingEnabled(),
                setting.isMinMarginProtectEnabled(),
                setting.isPriceAutoUpdateEnabled(),
                setting.isStopLossEnabled(),
                setting.getShippingFeeType(),
                setting.getBaseShippingFee(),
                setting.getRemoteAreaExtraShippingFee(),
                setting.getReturnShippingFee()
        );
    }

}

package com.example.team3Project.domain.product.processing.application;

import com.example.team3Project.domain.policy.application.PolicyQueryService;
import com.example.team3Project.domain.policy.dto.BlockedWordResponse;
import com.example.team3Project.domain.policy.dto.PolicyBundle;
import com.example.team3Project.domain.policy.dto.ProductNameProcessingResponse;
import com.example.team3Project.domain.policy.dto.ReplacementWordResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
// 상품 가공 서비스 클래스
public class ProductProcessingService {

    private final PolicyQueryService policyQueryService;

    // 가공에 사용할 정책 묶음을 가져오는 메서드
    public PolicyBundle getPolicyBundleForProcessing(Long userId) {

        return policyQueryService.getPolicyBundle(userId);
    }

    // 금지어 포함 여부를 검사하는 메서드
    // productName : 검사할 상품명
    // policyBundle : 가공에 사용할 정책 묶음
    public boolean containsBlockedWord(String productName, PolicyBundle policyBundle){
        for (BlockedWordResponse blockedWord : policyBundle.getBlockedWords()){
            if (productName.contains(blockedWord.getBlockedWord())){
                return true;    // 금지어가 포함되는 경우
            }
        }
        return false;    // 모든 금지어를 검사했으나 포함되지 않는 경우
    }

    // 치환어를 적용하여 가공된 상품명을 반환하는 메서드
    // productName : 검사할 상품명
    // policyBundle : 가공에 사용할 정책 묶음
    public String applyReplacementWords(String productName, PolicyBundle policyBundle) {
        String processedName = productName;

        // 치환어 적용 - List로 받은 치환어에 대해 for문으로 돌려 작업한다.
        for (ReplacementWordResponse replacementWord : policyBundle.getReplacementWords()) {
            processedName = processedName.replace(
                    // 원본 단어
                    replacementWord.getSourceWord(),
                    // 새 단어
                    replacementWord.getReplacementWord()
            );
        }
        return processedName;
    }


    // 상품명을 가공하는 메서드
    // 가공이 가능한 경우 가공된 상품명을 반환 금지어로 제외되는 경우 값을 비워서 반환
    public Optional<String> processProductName(Long userId, String productName){
        PolicyBundle policyBundle = getPolicyBundleForProcessing(userId);

        // 금지어가 포함되어 있는지 검사
        if(containsBlockedWord(productName, policyBundle)){
            return Optional.empty();
        }

        // 치환어 적용
        String processedName = applyReplacementWords(productName, policyBundle);
        // 가공된 상품명 Optional에 담아서 반환
        return Optional.of(processedName);
    }

    // 가공 결과를 응답 DTO에 담는 메서드
    public ProductNameProcessingResponse processProductNameResponse(Long userId, String productName) {
        Optional<String> processedResult = processProductName(userId, productName);

        // 금지어가 포함되어 있어 가공에서 제외된 경우
        if (processedResult.isEmpty()) {
            return new ProductNameProcessingResponse(true, null);
        }

        // 가공 결과를 반환
        return new ProductNameProcessingResponse(false, processedResult.get());
    }


}
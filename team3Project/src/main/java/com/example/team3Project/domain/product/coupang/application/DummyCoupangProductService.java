package com.example.team3Project.domain.product.coupang.application;

import com.example.team3Project.domain.policy.application.PolicySettingService;
import com.example.team3Project.domain.policy.entity.MarketCode;
import com.example.team3Project.domain.policy.exception.PolicySettingNotFoundException;
import com.example.team3Project.domain.product.coupang.dao.DummyCoupangProductRepository;
import com.example.team3Project.domain.product.coupang.entity.DummyCoupangProduct;
import com.example.team3Project.domain.product.coupang.entity.DummyCoupangProductImage;
import com.example.team3Project.domain.product.coupang.entity.DummyCoupangProductOption;
import com.example.team3Project.domain.product.registration.dao.DummyProductRegistrationRepository;
import com.example.team3Project.domain.product.registration.entity.DummyProductImage;
import com.example.team3Project.domain.product.registration.entity.DummyProductOption;
import com.example.team3Project.domain.product.registration.entity.DummyProductRegistration;
import com.example.team3Project.domain.product.registration.entity.RegistrationStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class DummyCoupangProductService {

    private final DummyCoupangProductRepository dummyCoupangProductRepository;
    private final DummyProductRegistrationRepository dummyProductRegistrationRepository;
    private final PolicySettingService policySettingService;

    // 등록 후보 1건을 정책 기준 수동 발행 규칙에 따라 더미 쿠팡 상품으로 등록한다.
    public DummyCoupangProduct publishManually(Long userId, Long registrationId) {
        validateManualPublishAllowed(userId);
        return publishRegistration(userId, registrationId);
    }

    // 가공 직후 자동 발행은 정책이 켜진 상태에서 내부 흐름만 사용한다.
    public DummyCoupangProduct publishAutomatically(Long userId, Long registrationId) {
        return publishRegistration(userId, registrationId);
    }

    private DummyCoupangProduct publishRegistration(Long userId, Long registrationId) {
        DummyProductRegistration registration = getPublishableRegistration(userId, registrationId);
        DummyCoupangProduct product = dummyCoupangProductRepository
                .findByUserIdAndSourceProductId(userId, registration.getSourceProductId())
                .orElseGet(() -> DummyCoupangProduct.create(registration));

        product.updateFromRegistration(registration);
        product.replaceOptions(toCoupangOptions(registration.getOptions()));
        product.replaceImages(toCoupangImages(registration.getImages()));
        registration.markRegistered();

        return dummyCoupangProductRepository.save(product);
    }

    // 여러 등록 후보를 한 번에 정책 기준 수동 발행 규칙에 따라 등록한다.
    public List<DummyCoupangProduct> publishAllManually(Long userId, List<Long> registrationIds) {
        validateManualPublishAllowed(userId);
        List<Long> distinctIds = new ArrayList<>(new LinkedHashSet<>(registrationIds));
        List<DummyCoupangProduct> products = new ArrayList<>();

        for (Long registrationId : distinctIds) {
            products.add(publishRegistration(userId, registrationId));
        }

        return products;
    }

    // 현재 로그인 사용자의 쿠팡 더미 상품 목록을 조회한다.
    @Transactional(readOnly = true)
    public List<DummyCoupangProduct> getProducts(Long userId) {
        return dummyCoupangProductRepository.findByUserIdOrderByDummyCoupangProductIdDesc(userId);
    }

    // 현재 로그인 사용자의 쿠팡 더미 상품 상세를 조회한다.
    @Transactional(readOnly = true)
    public DummyCoupangProduct getProduct(Long userId, Long dummyCoupangProductId) {
        return dummyCoupangProductRepository.findByDummyCoupangProductIdAndUserId(dummyCoupangProductId, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "상품을 찾을 수 없습니다. id=" + dummyCoupangProductId
                ));
    }

    // 마켓에 등록이 가능한 등록 후보인지 검증하고 사용자 소유 데이터만 가져온다.
    private DummyProductRegistration getPublishableRegistration(Long userId, Long registrationId) {
        DummyProductRegistration registration = dummyProductRegistrationRepository
                .findByDummyProductRegistrationIdAndUserId(registrationId, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "등록 상품을 찾을 수 없습니다. id=" + registrationId
                ));

        if (registration.getMarketCode() != MarketCode.COUPANG) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "쿠팡 마켓 등록 후보만 등록할 수 있습니다."
            );
        }

        if (registration.getRegistrationStatus() != RegistrationStatus.READY
                && registration.getRegistrationStatus() != RegistrationStatus.REGISTERED) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "등록 가능 상태의 상품만 쿠팡 더미 마켓에 발행할 수 있습니다."
            );
        }

        return registration;
    }

    private void validateManualPublishAllowed(Long userId) {
        try {
            boolean autoPublishEnabled = policySettingService
                    .getPolicySetting(userId, MarketCode.COUPANG)
                    .isAutoPublishEnabled();

            if (autoPublishEnabled) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "쿠팡 자동 발행 정책이 활성화되어 있어 수동 등록할 수 없습니다."
                );
            }
        } catch (PolicySettingNotFoundException e) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "쿠팡 정책 설정이 존재하지 않아 등록 방식을 확인할 수 없습니다."
            );
        }
    }

    // 등록 후보 옵션을 쿠팡 더미 상품 옵션으로 그대로 복사한다.
    private List<DummyCoupangProductOption> toCoupangOptions(List<DummyProductOption> options) {
        List<DummyCoupangProductOption> coupangOptions = new ArrayList<>();
        for (DummyProductOption option : options) {
            coupangOptions.add(
                    DummyCoupangProductOption.create(
                            option.getOptionAsin(),
                            option.getOptionDimensions(),
                            option.isSelected(),
                            option.getPrice(),
                            option.getCurrency(),
                            option.getStock()
                    )
            );
        }
        return coupangOptions;
    }

    // 등록 후보 이미지를 쿠팡 더미 상품 이미지로 그대로 복사한다.
    private List<DummyCoupangProductImage> toCoupangImages(List<DummyProductImage> images) {
        List<DummyCoupangProductImage> coupangImages = new ArrayList<>();
        for (DummyProductImage image : images) {
            coupangImages.add(
                    DummyCoupangProductImage.create(
                            image.getImageType(),
                            image.getOptionAsin(),
                            image.getImageUrl(),
                            image.getSortOrder()
                    )
            );
        }
        return coupangImages;
    }
}

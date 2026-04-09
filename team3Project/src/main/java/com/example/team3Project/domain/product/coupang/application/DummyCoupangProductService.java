package com.example.team3Project.domain.product.coupang.application;

import com.example.team3Project.domain.policy.entity.MarketCode;
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

    // 등록 후보 1건을 쿠팡 더미 상품으로 발행한다.
    public DummyCoupangProduct publish(Long userId, Long registrationId) {
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

    // 여러 등록 후보를 한 번에 쿠팡 더미 상품으로 발행한다.
    public List<DummyCoupangProduct> publishAll(Long userId, List<Long> registrationIds) {
        List<Long> distinctIds = new ArrayList<>(new LinkedHashSet<>(registrationIds));
        List<DummyCoupangProduct> products = new ArrayList<>();

        for (Long registrationId : distinctIds) {
            products.add(publish(userId, registrationId));
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
                        "쿠팡 더미 상품을 찾을 수 없습니다. id=" + dummyCoupangProductId
                ));
    }

    // 발행 가능한 등록 후보인지 검증하고 본인 소유 데이터만 가져온다.
    private DummyProductRegistration getPublishableRegistration(Long userId, Long registrationId) {
        DummyProductRegistration registration = dummyProductRegistrationRepository
                .findByDummyProductRegistrationIdAndUserId(registrationId, userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "발행할 등록 상품을 찾을 수 없습니다. id=" + registrationId
                ));

        if (registration.getMarketCode() != MarketCode.COUPANG) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "쿠팡 마켓 등록 후보만 발행할 수 있습니다."
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

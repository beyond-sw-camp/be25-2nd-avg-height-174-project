package com.example.team3Project.domain.sourcing.integration;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

import com.example.team3Project.domain.sourcing.DTO.SourcingDTO;
import com.example.team3Project.domain.sourcing.entity.Sourcing;
import com.example.team3Project.domain.sourcing.repository.SourcingRepository;
import com.example.team3Project.domain.sourcing.service.SourcingPersistOutcome;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 가공 서비스에 전달하는 하나의 서비스 클래스.
 * DB 저장·정규화 직후 가공 서버로 소싱 JSON을 POST합니다.
 * URL이 비어 있으면 아무 것도 하지 않습니다. 호출은 비동기라 업로드 API 응답은 지연되지 않습니다.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class SourcingProcessingWebhookService {

    private final SourcingRepository sourcingRepository;

    // 가공 서비스 수신 URL. 저장,정규화 직후 비동기로 JSON 전송.
    @Value("${sourcing.processing.webhook-url:}")
    private String webhookUrl;

    /** 가공 /ingest 가 기대하는 marketCode (업로드 JSON에 없을 때). */
    @Value("${sourcing.processing.default-market-code:US}")
    private String defaultMarketCode;

    private final RestTemplate restTemplate = new RestTemplate();

    // 소싱 데이터 저장 후 가공 서비스에 소싱 데이터 보내기.
    public void notifyAfterSave(Long userId, SourcingPersistOutcome outcome, SourcingDTO sourcingDTO) {
        if (!StringUtils.hasText(webhookUrl)) {
            log.debug("가공 웹훅 생략: sourcing.processing.webhook-url 미설정 sourcingId={}", outcome.sourcingId());
            return;
        }
        log.info("가공 웹훅 비동기 전송 큐잉 sourcingId={} userId={}", outcome.sourcingId(), userId);

        Long sourcingId = outcome.sourcingId();
        Sourcing normalized = sourcingRepository.findById(sourcingId).orElse(null);
        Map<String, Object> body = buildIngestBody(sourcingDTO, normalized);

        CompletableFuture.runAsync(() -> send(userId, sourcingId, body));
    }

    /**
     * 가공 서비스 ingest는 루트에 상품 필드가 와야 하며, {@code sourcing} 래퍼를 쓰면 바인딩이 전부 null이 됩니다.
     * 필드명은 JSON 스키마와 동일: marketCode, asin, brand, currency, price, title, url, url_image, images, variation.
     */
    /**
     * 정규화된 DB 데이터가 있으면 번역된 이미지(MinIO 키)를 우선 사용합니다.
     */
    private Map<String, Object> buildIngestBody(SourcingDTO d, Sourcing normalized) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("marketCode", defaultMarketCode);
        body.put("asin", d.getAsin());
        body.put("brand", d.getBrand());
        body.put("currency", d.getCurrency());
        body.put("price", effectivePrice(d));
        body.put("title", d.getTitle());
        body.put("url", resolveAmazonProductUrl(d.getUrl()));
        body.put("url_image", d.getUrlImage());

        if (normalized != null && normalized.getDescriptionImages() != null) {
            body.put("images", normalized.getDescriptionImages());
        } else {
            body.put("images", d.getImages());
        }

        body.put("variation", d.getVariation());
        return body;
    }

    private static BigDecimal effectivePrice(SourcingDTO sourcingDTO) {
        BigDecimal p = sourcingDTO.getPrice();
        if (p != null) {
            return p;
        }
        if (sourcingDTO.getVariation() == null) {
            return null;
        }
        return sourcingDTO.getVariation().stream()
                .map(SourcingDTO.VariationDTO::getPrice)
                .filter(x -> x != null && x.compareTo(BigDecimal.ZERO) > 0)
                .min(BigDecimal::compareTo)
                .orElse(null);
    }

    private static String resolveAmazonProductUrl(String url) {
        if (!StringUtils.hasText(url)) {
            return url;
        }
        String t = url.trim();
        if (t.startsWith("http://") || t.startsWith("https://")) {
            return t;
        }
        return "https://www.amazon.com" + t;
    }

    private void send(Long userId, Long sourcingId, Map<String, Object> body) {
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            headers.set("X-User-Id", String.valueOf(userId));
            HttpEntity<Map<String, Object>> entity = new HttpEntity<>(body, headers);
            ResponseEntity<String> res = restTemplate.postForEntity(webhookUrl.trim(), entity, String.class);
            log.info("가공 웹훅 HTTP 성공 sourcingId={} userId={} status={} url={}",
                    sourcingId, userId, res.getStatusCode(), webhookUrl.trim());
        } catch (RestClientException ex) {
            log.warn("가공 웹훅 HTTP 실패 sourcingId={} userId={} url={} err={}",
                    sourcingId, userId, webhookUrl, ex.getMessage());
        } catch (Exception ex) {
            log.error("가공 웹훅 전송 중 예외 sourcingId={} userId={}", sourcingId, userId, ex);
        }
    }
}

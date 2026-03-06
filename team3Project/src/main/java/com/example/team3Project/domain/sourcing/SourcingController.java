package com.example.team3Project.domain.sourcing;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/sourcing")
@RequiredArgsConstructor
public class SourcingController {

    private final SourcingService sourcingService;
    

    @GetMapping("/")
    public String mainHome() {
        return "Hello World!";
    }
    
    @GetMapping("/upload")
    public String uploadForm() {
        return "upload";
    }

    @PostMapping("/upload")
    public Map<String, Object> handleFileUpload(@RequestPart("file") 
                                    MultipartFile file,
                                    @RequestPart("data") SourcingDTO sourcingDTO) 
    {
        Map<String,Object> response = new HashMap<>();

        // 파일이 비어있다면 에러 메세지 띄우기.

        if (file.isEmpty()) {
            response.put("status", "error");
            response.put("message", "파일을 선택해 주세요.");
            return response;
        }
        
        List<String> errors = sourcingService.validateSourcingData(sourcingDTO);
        
        // service에서 에러가 뭐라도 하나 있더라면, 그거 확인 시키기
        if (!errors.isEmpty()) {
            response.put("status", "error");
            response.put("message", "데이터 검증 실패");
            response.put("errors", errors);
        }else {
            response.put("status", "success");
            response.put("receivedData", sourcingDTO);
        }
        
        sourcingService.saveSourcingData(sourcingDTO);

        return response;

    }

    // 단일 상품 검증
    @PostMapping("/validate")
    public ResponseEntity<Map<String, Object>> validateSingle(@RequestBody SourcingDTO sourcingDTO) {
        Map<String, Object> response = new HashMap<>();
        List<String> errors = sourcingService.validateSourcingData(sourcingDTO);

        if (errors.isEmpty()) {
            response.put("status", "valid");
            response.put("message", "데이터가 정상입니다.");
        } else {
            response.put("status", "invalid");
            response.put("message", "누락/비정상 데이터가 감지되었습니다.");
            response.put("errors", errors);
        }

        return ResponseEntity.ok(response);
    }

    // 여러 상품 일괄 검증
    @PostMapping("/validate/batch")
    public ResponseEntity<Map<String, Object>> validateBatch(@RequestBody List<SourcingDTO> items) {
        Map<String, Object> response = new HashMap<>();

        if (items == null || items.isEmpty()) {
            response.put("status", "error");
            response.put("message", "검증할 데이터가 없습니다.");
            return ResponseEntity.badRequest().body(response);
        }

        ValidationResultDTO result = sourcingService.validateBatch(items);

        response.put("status", "done");
        response.put("totalCount", items.size());
        response.put("validCount", result.getValidItems().size());
        response.put("invalidCount", result.getInvalidItems().size());
        response.put("validItems", result.getValidItems());
        response.put("invalidItems", result.getInvalidItems());

        return ResponseEntity.ok(response);
    }

}

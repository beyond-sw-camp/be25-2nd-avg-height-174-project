package com.example.team3Project.domain.product.api;

import com.example.team3Project.domain.product.application.ProductPageService;
import com.example.team3Project.domain.product.dto.ProductPageDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

// 상품 상세 페이지 컨트롤러 (Thymeleaf 뷰 반환)
@Controller
@RequiredArgsConstructor
@RequestMapping("/product")
public class ProductPageController {

    private final ProductPageService productPageService;

    // /product 접근 시 첫 번째 상품 페이지로 리다이렉트
    @GetMapping
    public String productDefault() {
        Long firstId = productPageService.getFirstProductId();
        return "redirect:/product/" + firstId;
    }

    // 상품 ID로 상세 페이지 조회
    @GetMapping("/{id}")
    public String productDetail(@PathVariable Long id, Model model) {
        ProductPageDto dto = productPageService.getProductPage(id);
        model.addAttribute("product", dto);
        return "product";
    }
}

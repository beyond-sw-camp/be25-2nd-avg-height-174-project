package com.example.team3Project.domain.home;

import com.example.team3Project.domain.user.User;
import com.example.team3Project.global.annotation.LoginUser;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String home(@LoginUser User user, Model model) {
        if (user == null) {
            return "home";
        }

        // Gateway가 내려준 사용자 식별 헤더를 기반으로 조회한 현재 사용자 정보를 홈 화면에 넘긴다.
        model.addAttribute("user", user);
        return "loginHome";
    }
}

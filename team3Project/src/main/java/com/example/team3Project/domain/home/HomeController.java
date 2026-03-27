package com.example.team3Project.domain.home;

// 필요한 클래스들을 import
import com.example.team3Project.domain.user.dto.SessionUser;
import com.example.team3Project.global.util.SessionUtils;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

// 이 클래스가 Spring MVC의 컨트롤러로 등록됨
@Controller
public class HomeController {

    // 루트 주소("/")로 GET 요청이 들어오면 이 메서드 실행
    @GetMapping("/")
    public String home(HttpServletRequest request, Model model) {

        // 로그인 정보를 session에서 가져옴
        SessionUser loginUser = SessionUtils.getLoginUser(request);

        // 로그인 정보가 없으면 home 페이지로 이동
        if (loginUser == null) {
            return "home";
        }

        // 로그인 정보가 있으면 loginHome 페이지로 이동
        model.addAttribute("user", loginUser);
        return "loginHome";
    }
}

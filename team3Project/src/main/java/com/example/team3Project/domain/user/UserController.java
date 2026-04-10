package com.example.team3Project.domain.user;

import com.example.team3Project.domain.user.dto.LoginRequest;
import com.example.team3Project.domain.user.dto.PasswordChangeRequest;
import com.example.team3Project.domain.user.dto.SignupRequest;
import com.example.team3Project.domain.user.dto.UserUpdateFormRequest;
import com.example.team3Project.domain.user.dto.UserWithdrawRequest;
import com.example.team3Project.global.annotation.LoginUser;
import com.example.team3Project.global.exception.LoginException;
import com.example.team3Project.global.jwt.JwtUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/users")
// 현재 인증 주체는 별도 로그인 서비스이며, 이 컨트롤러는 남아 있는 서버 렌더링 사용자 화면을 담당한다.
public class UserController {

    private static final String ACCESS_TOKEN_COOKIE = "token";
    private static final int ACCESS_TOKEN_MAX_AGE = 60 * 60 * 2;

    private final UserService userService;
    private final JwtUtil jwtUtil;

    @GetMapping("/login")
    public String loginForm(@RequestParam(defaultValue = "/") String redirectURL,
                            Model model) {
        if (!model.containsAttribute("loginRequest")) {
            model.addAttribute("loginRequest", new LoginRequest());
        }
        model.addAttribute("redirectURL", redirectURL);
        return "users/login";
    }

    @PostMapping("/login")
    public String login(@Valid @ModelAttribute LoginRequest loginRequest,
                        BindingResult bindingResult,
                        @RequestParam(defaultValue = "/") String redirectURL,
                        HttpServletResponse response,
                        Model model) {

        if (bindingResult.hasErrors()) {
            model.addAttribute("redirectURL", redirectURL);
            return "users/login";
        }

        try {
            User loginUser = userService.login(loginRequest);

            // 로그인에 성공한 사용자의 핵심 정보를 담아 Gateway가 검증할 access token을 발급한다.
            String accessToken = jwtUtil.createAccessToken(loginUser);
            addAccessTokenCookie(response, accessToken);

            log.info("로그인 성공: userId={}, redirectURL={}", loginUser.getId(), redirectURL);
            return "redirect:" + redirectURL;

        } catch (LoginException e) {
            model.addAttribute("errorType", e.getErrorType());
            model.addAttribute("loginRequest", loginRequest);
            model.addAttribute("redirectURL", redirectURL);
            return "users/login";
        }
    }

    @GetMapping("/signup")
    public String signupForm(Model model) {
        model.addAttribute("user", new SignupRequest());
        return "users/signup";
    }

    @PostMapping("/signup")
    public String signup(@Valid @ModelAttribute("user") SignupRequest signupRequest,
                         BindingResult bindingResult,
                         Model model) {

        if (bindingResult.hasErrors()) {
            return "users/signup";
        }

        try {
            userService.signup(signupRequest);
            log.info("회원가입 성공: username={}", signupRequest.getUsername());
            return "redirect:/users/login";

        } catch (IllegalStateException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "users/signup";
        }
    }

    @PostMapping("/logout")
    public String logout(HttpServletResponse response) {
        // 로그아웃 시에는 서버 세션을 지우는 대신 브라우저의 access token 쿠키를 만료시킨다.
        expireAccessTokenCookie(response);
        log.info("로그아웃 완료");
        return "redirect:/users/login";
    }

    @GetMapping("/me")
    public String myPage(@LoginUser User user, Model model) {
        if (user == null) {
            return "redirect:/users/login";
        }
        model.addAttribute("user", user);
        return "users/me";
    }

    @GetMapping("/update")
    public String updateForm(@LoginUser User user, Model model) {
        if (user == null) {
            return "redirect:/users/login";
        }
        UserUpdateFormRequest formRequest = new UserUpdateFormRequest();
        formRequest.setNickname(user.getNickname());
        formRequest.setEmail(user.getEmail());
        model.addAttribute("userForm", formRequest);

        if (!model.containsAttribute("passwordChangeRequest")) {
            model.addAttribute("passwordChangeRequest", new PasswordChangeRequest());
        }

        return "users/update";
    }

    @PostMapping("/update")
    public String update(@LoginUser User user,
                         @Valid @ModelAttribute("userForm") UserUpdateFormRequest formRequest,
                         BindingResult bindingResult,
                         Model model) {
        if (user == null) {
            return "redirect:/users/login";
        }

        if (bindingResult.hasErrors()) {
            return "users/update";
        }

        try {
            userService.updateUserInfo(user.getId(), formRequest);
            log.info("사용자 정보 수정 성공: userId={}", user.getId());
            return "redirect:/users/me";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "users/update";
        }
    }

    @GetMapping("/delete")
    public String deleteForm(@LoginUser User user, Model model) {
        if (user == null) {
            return "redirect:/users/login";
        }
        model.addAttribute("withdrawRequest", new UserWithdrawRequest());
        return "users/delete";
    }

    @PostMapping("/delete")
    public String delete(@LoginUser User user,
                         @Valid @ModelAttribute("withdrawRequest") UserWithdrawRequest withdrawRequest,
                         BindingResult bindingResult,
                         HttpServletResponse response,
                         Model model) {
        if (user == null) {
            return "redirect:/users/login";
        }

        if (bindingResult.hasErrors()) {
            return "users/delete";
        }

        try {
            userService.deleteUser(user.getId(), withdrawRequest.getPassword());
            // 회원 탈퇴 후에는 기존 로그인 토큰도 더 이상 사용할 수 없도록 즉시 만료시킨다.
            expireAccessTokenCookie(response);
            log.info("회원 탈퇴 완료: userId={}", user.getId());
            return "redirect:/users/login";
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "users/delete";
        }
    }

    @PostMapping("/update/password")
    public String changePassword(@LoginUser User user,
                                 @Valid @ModelAttribute("passwordChangeRequest") PasswordChangeRequest passwordRequest,
                                 BindingResult bindingResult,
                                 @ModelAttribute("userForm") UserUpdateFormRequest userForm,
                                 Model model) {
        if (user == null) {
            return "redirect:/users/login";
        }

        // 새 비밀번호와 확인 비밀번호가 일치하는지 먼저 검사한다.
        if (!passwordRequest.getNewPassword().equals(passwordRequest.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "passwordMismatch", "새 비밀번호와 확인 비밀번호가 일치하지 않습니다.");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("passwordChangeRequest", passwordRequest);
            model.addAttribute("passwordChangeError", true);
            return "users/update";
        }

        try {
            userService.changePassword(user.getId(), passwordRequest.getCurrentPassword(), passwordRequest.getNewPassword());
            model.addAttribute("passwordSuccessMessage", "비밀번호가 성공적으로 변경되었습니다.");
        } catch (IllegalArgumentException e) {
            model.addAttribute("passwordChangeRequest", passwordRequest);
            model.addAttribute("passwordChangeError", true);
            model.addAttribute("passwordErrorMessage", e.getMessage());
        }

        return "users/update";
    }

    @GetMapping("/find-id")
    public String findIdForm() {
        return "users/find-id";
    }

    @PostMapping("/find-id")
    public String findId(@RequestParam("email") String email, Model model) {
        try {
            String username = userService.findUsernameByEmail(email);
            model.addAttribute("successMessage", "회원님의 아이디는 [" + username + "] 입니다.");
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
        }
        return "users/find-id";
    }

    @GetMapping("/reset-pw")
    public String resetPasswordForm() {
        return "users/reset-pw";
    }

    @PostMapping("/reset-pw")
    public String resetPassword(@RequestParam("username") String username,
                                @RequestParam("email") String email,
                                Model model) {
        try {
            userService.resetPassword(username, email);
            model.addAttribute("successMessage", "임시 비밀번호가 이메일로 발송되었습니다.");
        } catch (IllegalArgumentException e) {
            model.addAttribute("errorMessage", e.getMessage());
        }
        return "users/reset-pw";
    }

    private void addAccessTokenCookie(HttpServletResponse response, String accessToken) {
        // Gateway 필터가 같은 이름의 쿠키를 읽어 JWT를 검증하므로 token 쿠키로 내려준다.
        Cookie cookie = new Cookie(ACCESS_TOKEN_COOKIE, accessToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(ACCESS_TOKEN_MAX_AGE);
        response.addCookie(cookie);
    }

    private void expireAccessTokenCookie(HttpServletResponse response) {
        Cookie cookie = new Cookie(ACCESS_TOKEN_COOKIE, "");
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}

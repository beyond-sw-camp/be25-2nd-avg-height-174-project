package com.example.team3Project.domain.user;

import com.example.team3Project.domain.user.dto.LoginRequest;
import com.example.team3Project.domain.user.dto.SignupRequest;
import com.example.team3Project.domain.user.dto.UserUpdateFormRequest;
import com.example.team3Project.domain.user.dto.UserUpdateRequest;
import com.example.team3Project.global.exception.LoginErrorType;
import com.example.team3Project.global.exception.LoginException;
import com.example.team3Project.global.util.EmailService;
import com.example.team3Project.global.util.VerificationCodeStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final VerificationCodeStore verificationCodeStore;
    private final EmailService emailService;

    @Transactional
    public User signup(SignupRequest request) {
        Optional<User> existingUser = userRepository.findByUsername(request.getUsername());
        if (existingUser.isPresent()) {
            throw new IllegalStateException("?��? 존재?�는 ?�이?�입?�다.");
        }

        User user = new User();
        user.setLoginId(request.getUsername());
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        user.setNickname(request.getNickname());

        return userRepository.save(user);
    }

    @Transactional
    public User login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new LoginException(LoginErrorType.USERNAME_NOT_FOUND));

        if (user.isLocked()) {
            log.warn("로그???�도 - ?�긴 계정: username={}", request.getUsername());
            throw new LoginException(LoginErrorType.ACCOUNT_LOCKED);
        }

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            user.increaseLoginFailCount();
            log.warn("로그???�패 - 비�?번호 불일�? username={}, ?�패?�수={}",
                    request.getUsername(), user.getLoginFailCount());

            if (user.isLocked()) {
                log.warn("계정 ?��? 처리: username={}", request.getUsername());
                throw new LoginException(LoginErrorType.ACCOUNT_LOCKED);
            }

            throw new LoginException(LoginErrorType.PASSWORD_MISMATCH);
        }

        if (user.getLoginFailCount() > 0) {
            user.resetLoginFailCount();
        }

        log.info("로그???�공: username={}", request.getUsername());
        return user;
    }

    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("?�용?��? 찾을 ???�습?�다."));

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("?�재 비�?번호가 ?�치?��? ?�습?�다.");
        }

        if (newPassword.length() < 8) {
            throw new IllegalArgumentException("??비�?번호??최소 8???�상?�어???�니??");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        log.info("비�?번호 변�??�료: userId={}", userId);
    }

    @Transactional
    public void unlockAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("?�용?��? 찾을 ???�습?�다."));

        user.unlock();
        log.info("계정 ?��? ?�제: userId={}", userId);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Transactional
    public User updateUser(Long userId, UserUpdateRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("?�용?��? 찾을 ???�습?�다."));

        user.setNickname(request.getNickname());
        log.info("?�용???�보 ?�정 ?�료: userId={}", userId);
        return user;
    }

    @Transactional
    public User updateUserInfo(Long userId, UserUpdateFormRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("?�용?��? 찾을 ???�습?�다."));

        user.setNickname(request.getNickname());
        user.setEmail(request.getEmail());
        user.setPhoneNumber(request.getPhoneNumber());
        log.info("사용자 프로필 수정 완료: userId={}", userId);
        return user;  // 추가
    }

    public void sendPasswordResetCode(String loginIdOrEmail) {
        User user = findUserByLoginIdOrEmail(loginIdOrEmail)
                .orElseThrow(() -> new LoginException(LoginErrorType.USER_NOT_FOUND));

        String code = verificationCodeStore.generateAndStore(user.getEmail());
        emailService.sendVerificationCode(user.getEmail(), code);
        log.info("비�?번호 ?�설???�증코드 발송: userId={}, email={}", user.getId(), user.getEmail());
    }

    public void verifyPasswordResetCode(String loginIdOrEmail, String code) {
        User user = findUserByLoginIdOrEmail(loginIdOrEmail)
                .orElseThrow(() -> new LoginException(LoginErrorType.USER_NOT_FOUND));

        boolean valid = verificationCodeStore.verifyAndConsume(user.getEmail(), code);
        if (!valid) {
            throw new IllegalArgumentException("?�효?��? ?�거??만료???�증코드?�니??");
        }
        verificationCodeStore.markAsVerified(user.getEmail());
        log.info("비�?번호 ?�설???�증코드 검�??�공: userId={}", user.getId());
    }

    @Transactional
    public void resetPassword(String loginIdOrEmail, String code, String newPassword) {
        User user = findUserByLoginIdOrEmail(loginIdOrEmail)
                .orElseThrow(() -> new LoginException(LoginErrorType.USER_NOT_FOUND));

        boolean valid = verificationCodeStore.verifyAndConsume(user.getEmail(), code);
        if (!valid) {
            throw new IllegalArgumentException("?�효?��? ?�거??만료???�증코드?�니??");
        }

        if (newPassword.length() < 8) {
            throw new IllegalArgumentException("??비�?번호??최소 8???�상?�어???�니??");
        }

        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new IllegalArgumentException("??비�?번호??기존 비�?번호?� ?�라???�니??");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.resetLoginFailCount();
        if (user.isLocked()) {
            user.unlock();
        }
        log.info("비�?번호 ?�설???�료: userId={}", user.getId());
    }

    @Transactional
    public void deleteUser(Long userId, String password) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new LoginException(LoginErrorType.USER_NOT_FOUND));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("비�?번호가 ?�치?��? ?�습?�다.");
        }

        userRepository.delete(user);
        log.info("?�원 ?�퇴 ?�료: userId={}", userId);
    }

    public String findUsernameByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("?�치?�는 ?�원 ?�보가 ?�습?�다."));
        return user.getUsername();
    }

    @Transactional
    public void resetPassword(String username, String email) {
        User user = userRepository.findByUsernameAndEmail(username, email)
                .orElseThrow(() -> new IllegalArgumentException("?�치?�는 ?�원 ?�보가 ?�습?�다."));

        String tempPassword = createTempPassword();

        user.setPassword(passwordEncoder.encode(tempPassword));
        user.resetLoginFailCount();
        if (user.isLocked()) {
            user.unlock();
        }

        emailService.sendTemporaryPassword(user.getEmail(), tempPassword);
        log.info("?�시 비�?번호 발급 ?�료: userId={}, email={}", user.getId(), user.getEmail());
    }

    private Optional<User> findUserByLoginIdOrEmail(String loginIdOrEmail) {
        Optional<User> byUsername = userRepository.findByUsername(loginIdOrEmail);
        if (byUsername.isPresent()) {
            return byUsername;
        }
        return userRepository.findByEmail(loginIdOrEmail);
    }

    private String createTempPassword() {
        return UUID.randomUUID().toString().replace("-", "").substring(0, 10);
    }
}

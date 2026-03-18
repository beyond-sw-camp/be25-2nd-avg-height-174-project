package com.example.team3Project.domain.policy.exception;

// 정책 설정 데이터가 없을 때 던질 예외 클래스
public class PolicySettingNotFoundException extends RuntimeException{
    public PolicySettingNotFoundException(String message) {
        super(message);
    }
}

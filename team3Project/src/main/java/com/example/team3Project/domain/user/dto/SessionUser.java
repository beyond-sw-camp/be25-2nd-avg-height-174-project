package com.example.team3Project.domain.user.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data // getter, setter, toString, equals, hashCode 자동 생성
@NoArgsConstructor // 기본 생성자 자동 생성
@AllArgsConstructor // 모든 필드를 파라미터로 받는 생성자 자동 생성
public class SessionUser implements Serializable {

    // 로그인한 사용자의 고유 ID
    private Long id;

    // 로그인한 사용자의 아이디(username)
    private String username;

    // 로그인한 사용자의 닉네임
    private String nickname;
}

package com.example.team3Project.domain.policy.dao;

import com.example.team3Project.domain.policy.entity.UserPolicySetting;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional; // 조회 결과가 없을 때에 사용 - null 대신 Optional().empty()로 사용가능
public interface UserPolicySettingRepository extends JpaRepository<UserPolicySetting, Long> {
    Optional<UserPolicySetting> findByUserId(Long userId);
}

package com.sso.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sso.domain.SsoUser;

/** SSO 사용자 JPA 리포지토리. username으로 조회. */
public interface SsoUserRepository extends JpaRepository<SsoUser, Long> {

    Optional<SsoUser> findByUsername(String username);
}

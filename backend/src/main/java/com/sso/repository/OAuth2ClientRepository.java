package com.sso.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sso.domain.OAuth2Client;

/** OAuth2 클라이언트 JPA 리포지토리. clientId로 조회. */
public interface OAuth2ClientRepository extends JpaRepository<OAuth2Client, Long> {

    Optional<OAuth2Client> findByClientId(String clientId);
}

package com.sso.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/** SSO 로그인 사용자 엔티티. username/비밀번호(암호화)/역할(쉼표 구분) 저장. */
@Entity
@Table(name = "sso_user")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SsoUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String username;

    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 500)
    private String roles = "USER";

    public SsoUser(String username, String password, String roles) {
        this.username = username;
        this.password = password;
        this.roles = roles != null ? roles : "USER";
    }

    public void setRoles(String roles) {
        this.roles = roles != null ? roles : "USER";
    }
}

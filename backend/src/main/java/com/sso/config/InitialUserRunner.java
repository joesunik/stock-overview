package com.sso.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import com.sso.domain.SsoUser;
import com.sso.repository.SsoUserRepository;

/** 기동 시 테스트 사용자 admin/password가 없으면 한 건 생성. */
@Component
public class InitialUserRunner implements ApplicationRunner {

    private final SsoUserRepository ssoUserRepository;
    private final PasswordEncoder passwordEncoder;

    public InitialUserRunner(SsoUserRepository ssoUserRepository, PasswordEncoder passwordEncoder) {
        this.ssoUserRepository = ssoUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /** 사용자 'admin'이 없을 때만 암호화된 비밀번호로 저장. */
    @Override
    public void run(ApplicationArguments args) {
        if (ssoUserRepository.findByUsername("admin").isEmpty()) {
            SsoUser user = new SsoUser("admin", passwordEncoder.encode("password"), "USER");
            ssoUserRepository.save(user);
        }
    }
}

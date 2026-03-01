package com.sso.service;

import java.util.stream.Collectors;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.sso.domain.SsoUser;
import com.sso.repository.SsoUserRepository;

/** Spring Security용 사용자 로드. DB에서 조회 후 roles를 ROLE_xxx 권한으로 변환. */
@Service
public class SsoUserDetailsService implements UserDetailsService {

    private final SsoUserRepository ssoUserRepository;

    public SsoUserDetailsService(SsoUserRepository ssoUserRepository) {
        this.ssoUserRepository = ssoUserRepository;
    }

    /** DB 사용자를 UserDetails로 변환. roles는 쉼표 구분, 앞에 ROLE_ 붙여 권한으로 사용. */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        SsoUser user = ssoUserRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        var authorities = java.util.Arrays.stream(user.getRoles().split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());
        return User.builder()
                .username(user.getUsername())
                .password(user.getPassword())
                .authorities(authorities)
                .build();
    }
}

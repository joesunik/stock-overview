package com.sso.web;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sso.domain.OAuth2Client;
import com.sso.repository.OAuth2ClientRepository;
import com.sso.web.dto.OAuth2ClientDto;
import com.sso.web.dto.UpdateRedirectUrisRequest;

/** OAuth2 클라이언트 목록 조회 및 redirect URI 수정 API (JWT 인증). */
@RestController
@RequestMapping("/api/admin/clients")
public class AdminClientController {

    private final OAuth2ClientRepository oauth2ClientRepository;

    public AdminClientController(OAuth2ClientRepository oauth2ClientRepository) {
        this.oauth2ClientRepository = oauth2ClientRepository;
    }

    /** 전체 클라이언트 목록을 DTO 리스트로 반환. */
    @GetMapping
    public List<OAuth2ClientDto> list() {
        return oauth2ClientRepository.findAll().stream()
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    /** 해당 클라이언트의 redirect URIs만 갱신. */
    @PatchMapping("/{id}")
    public ResponseEntity<OAuth2ClientDto> updateRedirectUris(
            @PathVariable Long id,
            @RequestBody UpdateRedirectUrisRequest body) {
        if (body == null || body.redirectUris() == null) {
            return ResponseEntity.badRequest().build();
        }
        return oauth2ClientRepository.findById(id)
                .map(client -> {
                    String joined = String.join(",", body.redirectUris());
                    client.setRedirectUris(joined);
                    oauth2ClientRepository.save(client);
                    return ResponseEntity.ok(toDto(client));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    private static List<String> split(String s) {
        if (s == null || s.isBlank()) return List.of();
        return Arrays.stream(s.split(","))
                .map(String::trim)
                .filter(x -> !x.isEmpty())
                .collect(Collectors.toList());
    }

    private OAuth2ClientDto toDto(OAuth2Client c) {
        return new OAuth2ClientDto(
                c.getId(),
                c.getClientId(),
                split(c.getRedirectUris()),
                split(c.getPostLogoutRedirectUris()),
                c.isRequireAuthorizationConsent()
        );
    }
}

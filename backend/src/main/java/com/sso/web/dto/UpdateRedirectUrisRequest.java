package com.sso.web.dto;

import java.util.List;

/** redirect URI 목록 수정 요청 body. */
public record UpdateRedirectUrisRequest(List<String> redirectUris) {
}

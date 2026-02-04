package com.stockoverview.controller;

import com.stockoverview.dto.AccountDto;
import com.stockoverview.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
public class AccountsController {

    private final AccountService accountService;

    @GetMapping
    public ResponseEntity<List<AccountDto>> listAccounts() {
        List<AccountDto> list = accountService.listAccounts();
        return ResponseEntity.ok(list);
    }

    @PostMapping
    public ResponseEntity<AccountDto> addAccount(@RequestBody Map<String, String> body) {
        String acctNo = body != null ? body.get("acctNo") : null;
        AccountDto created = accountService.addAccount(acctNo);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @PatchMapping("/{id}/default")
    public ResponseEntity<Void> setDefault(@PathVariable Long id) {
        accountService.setDefaultAccount(id);
        return ResponseEntity.noContent().build();
    }
}

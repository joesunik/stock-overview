package com.stockoverview.controller;

import com.stockoverview.dto.DailyBalanceDto;
import com.stockoverview.dto.MonthlySummaryResponse;
import com.stockoverview.service.AccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/daily-balance")
    public ResponseEntity<List<DailyBalanceDto>> getDailyBalance(
            @RequestParam(required = false) String acctNo,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        List<DailyBalanceDto> list = accountService.getDailyBalance(acctNo != null ? acctNo : "", startDate, endDate);
        return ResponseEntity.ok(list);
    }

    @GetMapping("/monthly-summary")
    public ResponseEntity<MonthlySummaryResponse> getMonthlySummary(
            @RequestParam(required = false) String acctNo,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        MonthlySummaryResponse response = accountService.getMonthlySummary(acctNo != null ? acctNo : "", startDate, endDate);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<Map<String, String>> refreshAccount(@RequestParam String acctNo) {
        accountService.refreshAccount(acctNo);
        return ResponseEntity.ok(Map.of("message", "새로고침 완료"));
    }

    @PostMapping("/refresh-foreign-futures")
    public ResponseEntity<Map<String, String>> refreshForeignAndFuturesData(
            @RequestParam String acctNo,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        accountService.refreshForeignAndFuturesData(acctNo, startDate, endDate);
        return ResponseEntity.ok(Map.of("message", "해외주식/선물 데이터 갱신 완료"));
    }
}

package com.stockoverview.service;

import com.stockoverview.dto.AccountDto;
import com.stockoverview.dto.DailyBalanceDto;
import com.stockoverview.dto.DepositWithdrawalSummaryDto;
import com.stockoverview.dto.MonthlySummaryDto;
import com.stockoverview.dto.MonthlySummaryResponse;
import com.stockoverview.dto.TransactionDto;
import com.stockoverview.entity.Account;
import com.stockoverview.entity.DailyBalance;
import com.stockoverview.kiwoom.KiwoomApiException;
import com.stockoverview.kiwoom.KiwoomDailyBalanceClient;
import com.stockoverview.kiwoom.KiwoomTransactionsClient;
import com.stockoverview.kiwoom.dto.Ka01690Response;
import com.stockoverview.repository.AccountRepository;
import com.stockoverview.repository.DailyBalanceRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.ResourceAccessException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AccountService {

    private static final DateTimeFormatter API_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final DateTimeFormatter OUTPUT_DATE = DateTimeFormatter.ISO_LOCAL_DATE;
    private static final int START_YEAR = 2015;

    private final KiwoomDailyBalanceClient dailyBalanceClient;
    private final KiwoomTransactionsClient transactionsClient;
    private final AccountRepository accountRepository;
    private final DailyBalanceRepository dailyBalanceRepository;

    @Transactional(readOnly = true)
    public List<AccountDto> listAccounts() {
        return accountRepository.findAllByOrderByIsDefaultDesc().stream()
                .map(a -> AccountDto.builder()
                        .id(a.getId())
                        .acctNo(a.getAcctNo())
                        .isDefault(Boolean.TRUE.equals(a.getIsDefault()))
                        .build())
                .toList();
    }

    @Transactional
    public AccountDto addAccount(String acctNo) {
        log.info("🆕 [addAccount] 계좌 등록 시작 - {}", acctNo);

        String trimmed = acctNo != null ? acctNo.trim() : "";
        if (trimmed.isEmpty()) {
            log.error("❌ [addAccount] 빈 계좌번호");
            throw new IllegalArgumentException("계좌번호를 입력하세요.");
        }

        if (accountRepository.findByAcctNo(trimmed).isPresent()) {
            log.error("❌ [addAccount] 중복된 계좌번호 - {}", trimmed);
            throw new IllegalArgumentException("이미 등록된 계좌번호입니다: " + trimmed);
        }

        boolean isFirst = accountRepository.count() == 0;
        log.info("📝 [addAccount] 계좌 데이터 생성 - {}, 기본계좌: {}", trimmed, isFirst);

        Account account = Account.builder()
                .acctNo(trimmed)
                .isDefault(isFirst)
                .build();
        account = accountRepository.save(account);
        log.info("💾 [addAccount] 계좌 저장 완료 - ID: {}, acctNo: {}", account.getId(), trimmed);

        LocalDate today = LocalDate.now();
        LocalDate start = LocalDate.of(START_YEAR, 1, 1);
        log.info("🔄 [addAccount] Kiwoom 데이터 조회 시작 - {} ~ {}", start, today);
        fetchAndSaveFromKiwoom(account.getId(), trimmed, start, today);
        log.info("🔄 [addAccount] Kiwoom 데이터 조회 완료");

        log.info("✅ [addAccount] 계좌 등록 완료 - ID: {}, acctNo: {}", account.getId(), trimmed);
        return AccountDto.builder()
                .id(account.getId())
                .acctNo(account.getAcctNo())
                .isDefault(Boolean.TRUE.equals(account.getIsDefault()))
                .build();
    }

    @Transactional
    public void setDefaultAccount(Long accountId) {
        Account account = accountRepository.findById(accountId)
                .orElseThrow(() -> new IllegalArgumentException("계좌를 찾을 수 없습니다: " + accountId));
        accountRepository.findAll().forEach(a -> a.setIsDefault(false));
        accountRepository.flush();
        account.setIsDefault(true);
        accountRepository.save(account);
    }

    @Transactional
    public void refreshAccount(String acctNo) {
        String trimmed = acctNo != null ? acctNo.trim() : "";
        Account account = accountRepository.findByAcctNo(trimmed)
                .orElseThrow(() -> new IllegalArgumentException("계좌를 찾을 수 없습니다: " + trimmed));

        LocalDate today = LocalDate.now();
        LocalDate start;
        var maxDateOpt = dailyBalanceRepository.findMaxBalanceDateByAccountId(account.getId());
        if (maxDateOpt.isEmpty()) {
            start = LocalDate.of(START_YEAR, 1, 1);
        } else {
            start = maxDateOpt.get().plusDays(1);
            if (start.isAfter(today)) {
                log.info("Account {} already up to date", trimmed);
                return;
            }
        }
        fetchAndSaveFromKiwoom(account.getId(), trimmed, start, today);
    }

    /**
     * 월별로 수집: 각 월의 마지막 영업일 1회만 ka01690 호출.
     * 기간 내 월 단위 루프, 해당 월 말 영업일이 startDate 이상 endDate 이하이고 아직 없을 때만 호출.
     */
    public void fetchAndSaveFromKiwoom(Long accountId, String acctNo, LocalDate startDate, LocalDate endDate) {
        log.info("📊 [fetchAndSaveFromKiwoom] 시작 - 계좌: {}, 기간: {} ~ {}", acctNo, startDate, endDate);

        if (startDate.isAfter(endDate)) {
            log.warn("⚠️ 잘못된 날짜 범위 - startDate > endDate");
            return;
        }

        List<DailyBalance> existing = dailyBalanceRepository.findByAccountIdAndBalanceDateBetweenOrderByBalanceDateAsc(
                accountId, startDate, endDate);
        Set<LocalDate> existingDates = existing.stream().map(DailyBalance::getBalanceDate).collect(Collectors.toSet());
        log.debug("📦 기존 저장 데이터 개수: {} ({} ~ {})", existing.size(), startDate, endDate);

        List<DailyBalance> toSave = new ArrayList<>();
        YearMonth startYm = YearMonth.from(startDate);
        YearMonth endYm = YearMonth.from(endDate);
        int monthCount = (int) java.time.temporal.ChronoUnit.MONTHS.between(startYm, endYm) + 1;
        log.info("🔄 월별 처리 시작 - {} 개월 ({} ~ {})", monthCount, startYm, endYm);

        int processedCount = 0;
        int skippedCount = 0;

        for (YearMonth ym = startYm; !ym.isAfter(endYm); ym = ym.plusMonths(1)) {
            LocalDate lastDay = ym.atEndOfMonth();
            if (lastDay.isAfter(endDate)) {
                lastDay = endDate;
            }
            while (lastDay.getDayOfWeek() == DayOfWeek.SATURDAY || lastDay.getDayOfWeek() == DayOfWeek.SUNDAY) {
                lastDay = lastDay.minusDays(1);
            }

            log.debug("📅 [{}/{}] 월 처리: {} ({})", ++processedCount, monthCount, ym, lastDay.getDayOfWeek());

            if (lastDay.isBefore(startDate) || existingDates.contains(lastDay)) {
                log.debug("  ⏭️ 스킵 - 이미 저장됨 또는 범위 밖");
                skippedCount++;
                continue;
            }

            String qryDt = lastDay.format(API_DATE);
            final int maxAttempts = 3;
            boolean success = false;

            for (int attempt = 0; attempt < maxAttempts && !success; attempt++) {
                try {
                    log.debug("  🔗 API 호출 시도 [{}/{} 시도] - {}", attempt + 1, maxAttempts, qryDt);

                    // 1. 국내주식 (ka01690)
                    Ka01690Response domestic = dailyBalanceClient.fetchDailyBalance(acctNo, qryDt);

                    success = true;

                    if (domestic.getReturnCode() != null && domestic.getReturnCode() != 0) {
                        log.warn("  ❌ API 오류 (국내주식) - returnCode: {}, message: {}", domestic.getReturnCode(), domestic.getReturnMsg());
                    } else {
                        DailyBalance entity = mergeAllData(accountId, lastDay, domestic);
                        if (entity != null) {
                            toSave.add(entity);
                            existingDates.add(lastDay);
                            log.debug("  ✅ 데이터 저장 대기 - {} (누계: {})", lastDay, toSave.size());
                        } else {
                            log.debug("  ⚠️ 응답 데이터 공백 - {}", lastDay);
                        }
                    }

                    try {
                        Thread.sleep(800);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        log.warn("  ⏹️ 스레드 중단됨");
                        break;
                    }
                } catch (KiwoomApiException e) {
                    log.warn("  ❌ Kiwoom API 예외 - {}: {} (재시도 없음)", qryDt, e.getMessage());
                    success = true;
                } catch (ResourceAccessException e) {
                    if (attempt == maxAttempts - 1) {
                        log.warn("  ❌ 네트워크 오류 - {} ({} 회 재시도 후 포기): {}", qryDt, maxAttempts, e.getMessage());
                    } else {
                        log.warn("  ⚠️ 네트워크 오류 - {}: {} ({}초 후 재시도...)", qryDt, e.getMessage(), 1.5);
                        try {
                            Thread.sleep(1500);
                        } catch (InterruptedException ie) {
                            Thread.currentThread().interrupt();
                            log.warn("  ⏹️ 재시도 대기 중 스레드 중단");
                            break;
                        }
                    }
                } catch (Exception e) {
                    log.error("  🔴 예상 밖의 오류 - {}", qryDt, e);
                    throw e;
                }
            }
        }

        log.info("💾 데이터 저장 시작 - 저장 대기: {} 건, 스킵: {} 건", toSave.size(), skippedCount);
        if (!toSave.isEmpty()) {
            dailyBalanceRepository.saveAll(toSave);
            log.info("✨ [fetchAndSaveFromKiwoom] 완료 - 계좌: {}, 저장됨: {} 건 ({} ~ {})", acctNo, toSave.size(), startDate, endDate);
        } else {
            log.info("📭 [fetchAndSaveFromKiwoom] 완료 - 저장할 데이터 없음");
        }
    }

    private static DailyBalance mergeAllData(Long accountId, LocalDate date, Ka01690Response domestic) {
        String dayStkAsst = domestic.getDayStkAsst();
        String dbstBal = domestic.getDbstBal();
        String totEvltAmt = domestic.getTotEvltAmt();

        if (dayStkAsst == null && dbstBal == null && totEvltAmt == null) {
            return null;
        }

        return DailyBalance.builder()
                .accountId(accountId)
                .balanceDate(date)
                .estimatedAsset(parseDecimal(dayStkAsst))
                .depositBalance(parseDecimal(dbstBal))
                .totalEvltAmt(parseDecimal(totEvltAmt))
                .profitRate(domestic.getTotPrftRt())
                .build();
    }

    @Transactional(readOnly = true)
    public List<DailyBalanceDto> getDailyBalance(String acctNo, LocalDate startDate, LocalDate endDate) {
        if (startDate.isAfter(endDate)) {
            return List.of();
        }
        var accountOpt = accountRepository.findByAcctNo(acctNo != null ? acctNo.trim() : "");
        if (accountOpt.isEmpty()) {
            return List.of();
        }
        Long accountId = accountOpt.get().getId();
        List<DailyBalance> list = dailyBalanceRepository.findByAccountIdAndBalanceDateBetweenOrderByBalanceDateAsc(
                accountId, startDate, endDate);
        return list.stream()
                .map(db -> DailyBalanceDto.builder()
                        .date(db.getBalanceDate().format(OUTPUT_DATE))
                        .estimatedAsset(db.getEstimatedAsset())
                        .depositBalance(db.getDepositBalance())
                        .totalEvltAmt(db.getTotalEvltAmt())
                        .profitRate(db.getProfitRate())
                        .build())
                .toList();
    }

    private static BigDecimal parseDecimal(String s) {
        if (s == null || s.isBlank()) {
            return null;
        }
        try {
            return new BigDecimal(s.trim().replace(",", ""));
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Transactional(readOnly = true)
    public List<TransactionDto> getTransactions(String acctNo, LocalDate startDate, LocalDate endDate) {
        String trimmed = acctNo != null ? acctNo.trim() : "";
        if (trimmed.isEmpty()) {
            return List.of();
        }

        String strtDt = startDate.format(API_DATE);
        String endDt = endDate.format(API_DATE);

        log.info("📋 거래 내역 조회 - acctNo: {}, 기간: {} ~ {}", trimmed, strtDt, endDt);

        try {
            List<Map<String, Object>> rawTransactions = transactionsClient.fetchTransactions(trimmed, strtDt, endDt);

            return rawTransactions.stream()
                    .map(this::mapToTransactionDto)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.warn("거래 내역 조회 실패 - {}: {}", trimmed, e.getMessage());
            return List.of();
        }
    }

    private TransactionDto mapToTransactionDto(Map<String, Object> raw) {
        // API 응답 구조에 따라 매핑 필요 (예상 필드명)
        String date = (String) raw.getOrDefault("trd_dt", "");
        String type = (String) raw.getOrDefault("trd_tp", "");
        String amount = (String) raw.getOrDefault("trd_amt", "0");
        String remark = (String) raw.getOrDefault("remark", "");

        return TransactionDto.builder()
                .date(date.length() >= 8 ? date.substring(0, 4) + "-" + date.substring(4, 6) + "-" + date.substring(6, 8) : date)
                .type(type)
                .amount(parseDecimal(amount))
                .remark(remark)
                .build();
    }

    @Transactional(readOnly = true)
    public DepositWithdrawalSummaryDto getDepositWithdrawalSummary(String acctNo, LocalDate startDate, LocalDate endDate) {
        String trimmed = acctNo != null ? acctNo.trim() : "";
        if (trimmed.isEmpty()) {
            return DepositWithdrawalSummaryDto.builder()
                    .totalDeposit(BigDecimal.ZERO)
                    .totalWithdrawal(BigDecimal.ZERO)
                    .build();
        }

        String strtDt = startDate.format(API_DATE);
        String endDt = endDate.format(API_DATE);

        log.info("💰 입출금 요약 조회 - acctNo: {}, 기간: {} ~ {}", trimmed, strtDt, endDt);

        try {
            List<Map<String, Object>> rawTransactions = transactionsClient.fetchTransactions(trimmed, strtDt, endDt);

            BigDecimal totalDeposit = BigDecimal.ZERO;
            BigDecimal totalWithdrawal = BigDecimal.ZERO;

            for (Map<String, Object> transaction : rawTransactions) {
                String type = (String) transaction.getOrDefault("trd_tp", "");
                String amount = (String) transaction.getOrDefault("trd_amt", "0");
                BigDecimal amountValue = parseDecimal(amount);

                if (amountValue != null) {
                    // 입금/출금 구분 (실제 API 응답 형식에 맞춰 수정 필요)
                    if ("입금".equals(type) || "D".equals(type)) {
                        totalDeposit = totalDeposit.add(amountValue);
                    } else if ("출금".equals(type) || "W".equals(type)) {
                        totalWithdrawal = totalWithdrawal.add(amountValue);
                    }
                }
            }

            return DepositWithdrawalSummaryDto.builder()
                    .totalDeposit(totalDeposit)
                    .totalWithdrawal(totalWithdrawal)
                    .build();
        } catch (Exception e) {
            log.warn("입출금 요약 조회 실패 - {}: {}", trimmed, e.getMessage());
            return DepositWithdrawalSummaryDto.builder()
                    .totalDeposit(BigDecimal.ZERO)
                    .totalWithdrawal(BigDecimal.ZERO)
                    .build();
        }
    }

    @Transactional(readOnly = true)
    public MonthlySummaryResponse getMonthlySummary(String acctNo, LocalDate startDate, LocalDate endDate) {
        List<DailyBalanceDto> daily = getDailyBalance(acctNo, startDate, endDate);
        if (daily.isEmpty()) {
            return MonthlySummaryResponse.builder()
                    .startDate(startDate.format(OUTPUT_DATE))
                    .endDate(endDate.format(OUTPUT_DATE))
                    .monthlySummaries(List.of())
                    .build();
        }

        BigDecimal startAsset = daily.get(0).getEstimatedAsset();
        BigDecimal endAsset = daily.get(daily.size() - 1).getEstimatedAsset();
        String cumulativeReturnPct = null;
        BigDecimal cumulativeChange = null;
        if (startAsset != null && startAsset.compareTo(BigDecimal.ZERO) > 0 && endAsset != null) {
            cumulativeChange = endAsset.subtract(startAsset);
            cumulativeReturnPct = cumulativeChange.multiply(BigDecimal.valueOf(100)).divide(startAsset, 2, RoundingMode.HALF_UP).toString() + "%";
        }

        var byMonth = daily.stream()
                .collect(Collectors.groupingBy(d -> d.getDate().substring(0, 7)));
        List<MonthlySummaryDto> monthlySummaries = new ArrayList<>();
        for (var e : byMonth.entrySet()) {
            List<DailyBalanceDto> monthList = e.getValue();
            monthList.sort((a, b) -> a.getDate().compareTo(b.getDate()));
            BigDecimal monthStart = monthList.get(0).getEstimatedAsset();
            BigDecimal monthEnd = monthList.get(monthList.size() - 1).getEstimatedAsset();
            BigDecimal monthlyChange = (monthStart != null && monthEnd != null) ? monthEnd.subtract(monthStart) : null;
            String monthlyReturnPct = null;
            if (monthStart != null && monthStart.compareTo(BigDecimal.ZERO) > 0 && monthlyChange != null) {
                monthlyReturnPct = monthlyChange.multiply(BigDecimal.valueOf(100)).divide(monthStart, 2, RoundingMode.HALF_UP).toString() + "%";
            }
            monthlySummaries.add(MonthlySummaryDto.builder()
                    .yearMonth(e.getKey())
                    .startAsset(monthStart)
                    .endAsset(monthEnd)
                    .monthlyChange(monthlyChange)
                    .monthlyReturnPct(monthlyReturnPct)
                    .build());
        }
        monthlySummaries.sort((a, b) -> a.getYearMonth().compareTo(b.getYearMonth()));

        return MonthlySummaryResponse.builder()
                .startDate(startDate.format(OUTPUT_DATE))
                .endDate(endDate.format(OUTPUT_DATE))
                .startAsset(startAsset)
                .endAsset(endAsset)
                .cumulativeReturnPct(cumulativeReturnPct)
                .cumulativeChange(cumulativeChange)
                .monthlySummaries(monthlySummaries)
                .build();
    }
}

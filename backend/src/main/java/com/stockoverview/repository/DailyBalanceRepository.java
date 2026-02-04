package com.stockoverview.repository;

import com.stockoverview.entity.DailyBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface DailyBalanceRepository extends JpaRepository<DailyBalance, Long> {

    List<DailyBalance> findByAccountIdAndBalanceDateBetweenOrderByBalanceDateAsc(
            Long accountId, LocalDate start, LocalDate end);

    @Query("SELECT MAX(db.balanceDate) FROM DailyBalance db WHERE db.accountId = :accountId")
    Optional<LocalDate> findMaxBalanceDateByAccountId(@Param("accountId") Long accountId);
}

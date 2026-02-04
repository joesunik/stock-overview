# addAccount() 메서드 로직 플로우

## 메서드 개요
`AccountService.addAccount()` 메서드는 새로운 계좌를 등록하는 메서드입니다.

---

## 실행 흐름도

```
┌─────────────────┐
│     시작        │
└────────┬────────┘
         │
         ▼
┌─────────────────────────────────┐
│   1단계: 입력값 검증             │
│   acctNo != null ? trim() : ""   │
│   → 공백 제거 및 null 처리       │
└────────┬────────────────────────┘
         │
         ▼
┌─────────────────────────────────┐
│   2단계: 공백 여부 확인          │
│   if (trimmed.isEmpty())        │
└────────┬────────────────────────┘
         │
    ┌────┴─────┐
    │           │
   YES         NO
    │           │
    ▼           ▼
  ❌          ┌──────────────────────────────────┐
  예외         │   3단계: 중복 계좌번호 확인      │
  발생        │   accountRepository.            │
  "계좌번호를 │    findByAcctNo(trimmed)       │
   입력하     │    .isPresent()                 │
   세요."     └────────┬──────────────────────────┘
              │
         ┌────┴──────┐
         │            │
        YES          NO
         │            │
         ▼            ▼
       ❌           ┌──────────────────────────────────┐
       예외        │  4단계: 기본 계좌 여부 판정       │
       발생        │  boolean isFirst =               │
       "이미      │    accountRepository.count() == 0│
       등록된      │  → isDefault 값 설정             │
       계좌번호   │    (첫 계좌: true, 아님: false) │
       입니다"   └────────┬──────────────────────────┘
                  │
                  ▼
          ┌──────────────────────────────┐
          │  5단계: Account 생성 및 저장  │
          │  Account.builder()           │
          │    .acctNo(trimmed)          │
          │    .isDefault(isFirst)       │
          │    .build()                  │
          │  → accountRepository.save()  │
          └────────┬─────────────────────┘
                   │
                   ▼
          ┌──────────────────────────────┐
          │  6단계: Kiwoom 데이터 조회   │
          │  LocalDate today = now()     │
          │  LocalDate start = 1월 1일   │
          │  (START_YEAR ~ 오늘)         │
          │  → fetchAndSaveFromKiwoom()  │
          │    (주식 데이터 조회 저장)    │
          └────────┬─────────────────────┘
                   │
                   ▼
          ┌──────────────────────────────┐
          │  7단계: AccountDto 생성      │
          │  AccountDto.builder()        │
          │    .id(account.getId())      │
          │    .acctNo(...)              │
          │    .isDefault(...)           │
          │    .build()                  │
          └────────┬─────────────────────┘
                   │
                   ▼
          ┌──────────────────────────────┐
          │   AccountDto 반환             │
          └──────────────────────────────┘
```

---

## 단계별 상세 설명

### 📋 1단계: 입력값 검증
```java
String trimmed = acctNo != null ? acctNo.trim() : "";
```
- null 체크를 통해 안전하게 처리
- 입력값의 좌우 공백 제거

### ❌ 2단계: 공백 여부 확인
```java
if (trimmed.isEmpty()) {
    throw new IllegalArgumentException("계좌번호를 입력하세요.");
}
```
- 빈 문자열 체크
- **예외 발생: 메서드 종료**

### ❌ 3단계: 중복 계좌번호 확인
```java
if (accountRepository.findByAcctNo(trimmed).isPresent()) {
    throw new IllegalArgumentException("이미 등록된 계좌번호입니다: " + trimmed);
}
```
- 데이터베이스에서 이미 존재하는지 확인
- **예외 발생: 메서드 종료**

### 🔄 4단계: 기본 계좌 여부 판정
```java
boolean isFirst = accountRepository.count() == 0;
```
- 현재 등록된 계좌 개수가 0이면 `true`
- 첫 번째 계좌는 기본 계좌로 설정

### 💾 5단계: Account 생성 및 저장
```java
Account account = Account.builder()
        .acctNo(trimmed)
        .isDefault(isFirst)
        .build();
account = accountRepository.save(account);
```
- Account 엔티티 생성
- 데이터베이스에 저장
- ID 값이 부여됨

### 📊 6단계: Kiwoom에서 데이터 조회
```java
LocalDate today = LocalDate.now();
LocalDate start = LocalDate.of(START_YEAR, 1, 1);
fetchAndSaveFromKiwoom(account.getId(), trimmed, start, today);
```
- 현재 날짜까지의 데이터 조회
- 계좌별 주식 정보 저장

#### fetchAndSaveFromKiwoom() 상세 흐름

```
┌─────────────────────────────────────┐
│ fetchAndSaveFromKiwoom() 시작        │
│ (accountId, acctNo, startDate, end) │
└────────────┬────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────────────┐
│  0단계: 날짜 범위 검증                           │
│  if (startDate.isAfter(endDate))                │
│  → 반환 (처리할 데이터 없음)                      │
└────────────┬────────────────────────────────────┘
             │ (startDate <= endDate)
             ▼
┌─────────────────────────────────────────────────┐
│  1단계: 기존 데이터 조회                          │
│  dailyBalanceRepository.                        │
│    findByAccountIdAndBalanceDateBetween         │
│    (accountId, startDate, endDate)              │
│  → Set<LocalDate> existingDates 생성            │
│    (이미 저장된 날짜들)                          │
└────────────┬────────────────────────────────────┘
             │
             ▼
┌─────────────────────────────────────────────────┐
│  2단계: 월별 루프 시작                            │
│  YearMonth startYm = YearMonth.from(startDate)  │
│  YearMonth endYm = YearMonth.from(endDate)      │
│  for (YearMonth ym = startYm; !ym.isAfter(endYm))│
│       → 시작월부터 끝월까지 반복                 │
└────────────┬────────────────────────────────────┘
             │
             ▼
┌────────────────────────────────────────────┐
│  3단계: 각 월의 마지막 영업일 찾기          │
│  LocalDate lastDay = ym.atEndOfMonth()     │
│                                            │
│  - endDate를 초과하면 endDate로 조정        │
│  - 토요일/일요일 제외 (영업일 찾기)         │
│    while (lastDay.getDayOfWeek() ==        │
│      SATURDAY || SUNDAY)                   │
│      lastDay = lastDay.minusDays(1)        │
└────────────┬───────────────────────────────┘
             │
             ▼
┌────────────────────────────────────────────┐
│  4단계: 저장 필요 여부 확인                  │
│  if (lastDay.isBefore(startDate) ||        │
│      existingDates.contains(lastDay))      │
│    → continue (스킵)                        │
└────────────┬───────────────────────────────┘
             │ (저장 필요)
             ▼
┌────────────────────────────────────────────┐
│  5단계: Kiwoom API 호출 (재시도 로직)       │
│  최대 3회 시도                             │
│  String qryDt = lastDay.format(API_DATE)   │
│  Ka01690Response resp =                    │
│    dailyBalanceClient.fetchDailyBalance()  │
└────────────┬───────────────────────────────┘
             │
      ┌──────┴──────────────────────────┐
      │                                 │
      ▼                                 ▼
  ┌─────────────┐           ┌─────────────────────┐
  │  성공        │           │  예외 발생           │
  │ success=true│           │                     │
  │             │           │ - KiwoomApiException│
  │             │           │   → 재시도 안함      │
  │             │           │                     │
  │             │           │ - ResourceAccess    │
  │             │           │   → 최대 3회 재시도 │
  │             │           │   (1.5초 대기)      │
  │             │           │                     │
  │             │           │ - 기타 예외         │
  │             │           │   → throw           │
  └──────┬──────┘           └──────────┬──────────┘
         │                            │
         ▼                            ▼
┌──────────────────────────────────────────────┐
│  6단계: 응답 검증 및 데이터 변환               │
│  if (resp.getReturnCode() != 0)              │
│    → 경고 로그 (저장 안함)                    │
│  else                                        │
│    → DailyBalance 엔티티로 변환               │
│    → toSave 리스트에 추가                    │
│    → existingDates에 날짜 추가               │
└──────────┬─────────────────────────────────┘
           │
           ▼
┌──────────────────────────────────────────────┐
│  7단계: 요청 간 대기                          │
│  Thread.sleep(800ms)                        │
│  (API 서버 과부하 방지)                       │
└──────────┬─────────────────────────────────┘
           │
           ▼
┌──────────────────────────────────────────────┐
│  월별 루프 반복                               │
│  (다음 월로 이동)                             │
└──────────┬─────────────────────────────────┘
           │
    ┌──────┴──────────────┐
    │                     │
   YES                   NO
(더 남은 월 있음)     (모든 월 처리)
    │                     │
    └─────────────────→───┘
                      │
                      ▼
        ┌──────────────────────────────────┐
        │  8단계: 한 번에 모두 저장         │
        │  if (!toSave.isEmpty())          │
        │    → dailyBalanceRepository      │
        │        .saveAll(toSave)          │
        │    → 로그 (저장된 행 수 출력)    │
        └──────────┬───────────────────────┘
                   │
                   ▼
        ┌──────────────────────────────────┐
        │  fetchAndSaveFromKiwoom() 종료   │
        └──────────────────────────────────┘
```

##### fetchAndSaveFromKiwoom() 핵심 로직

| 단계 | 처리 내용 | 특징 |
|------|---------|------|
| **0** | 날짜 범위 검증 | startDate > endDate면 즉시 반환 |
| **1** | 기존 데이터 로드 | DB에서 이미 저장된 날짜 조회 |
| **2** | 월별 루프 | 시작월 ~ 끝월까지 순회 |
| **3** | 영업일 찾기 | 각 월의 마지막 영업일 (토일 제외) |
| **4** | 스킵 판정 | 이미 존재하거나 범위 밖이면 스킵 |
| **5** | API 호출 | 최대 3회 재시도 로직 포함 |
| **6** | 데이터 변환 | Ka01690Response → DailyBalance 엔티티 |
| **7** | 대기 | 800ms 대기 (API 부하 관리) |
| **8** | 일괄 저장 | 모든 데이터를 한 번에 저장 |

##### 데이터 변환 (toDailyBalanceEntity)

```java
DailyBalance 엔티티 생성 시 필드:
├─ accountId        → 계좌 ID
├─ balanceDate      → 조회 날짜 (마지막 영업일)
├─ estimatedAsset   → 주식평가액 (dayStkAsst)
├─ depositBalance   → 예탁금 잔액 (dbstBal)
├─ totalEvltAmt     → 총평가금액 (totEvltAmt)
└─ profitRate       → 수익률 (totPrftRt)

📝 null 처리:
   - 세 필드 모두 null이면 엔티티 생성 안 함
   - 숫자 문자열은 parseDecimal()로 변환
   - 쉼표(,) 제거 후 BigDecimal로 파싱
```

##### 예외 처리 전략

| 예외 타입 | 발생 원인 | 처리 방식 |
|---------|---------|---------|
| **KiwoomApiException** | 키움 API 비즈니스 오류 | 1회 시도 후 스킵 (재시도 없음) |
| **ResourceAccessException** | 네트워크 I/O 오류 | 최대 3회 재시도 (1.5초 대기) |
| **InterruptedException** | 스레드 중단 | 중단 플래그 설정 및 루프 탈출 |
| **기타 예외** | 예상 밖의 오류 | throw (트랜잭션 롤백) |

##### 성능 특징

- ⚡ **배치 저장**: 월별로 수집한 모든 데이터를 마지막에 한 번에 저장 (DB 부하 감소)
- 🔁 **중복 방지**: existingDates Set으로 이미 저장된 날짜 빠르게 확인
- ⏰ **속도 조절**: API 호출 사이 800ms 대기로 서버 과부하 방지
- 🛡️ **재시도 로직**: 네트워크 오류에 대해서만 3회까지 재시도
- 📊 **월 단위**: 각 월의 마지막 영업일 1회만 호출 (데이터 최소화)

### 📦 7단계: AccountDto 생성 및 반환
```java
return AccountDto.builder()
        .id(account.getId())
        .acctNo(account.getAcctNo())
        .isDefault(Boolean.TRUE.equals(account.getIsDefault()))
        .build();
```
- 데이터베이스 엔티티를 DTO로 변환
- 클라이언트에게 반환

---

## 예외 처리

| 단계 | 조건 | 예외 메시지 |
|------|------|-----------|
| 2단계 | 빈 계좌번호 | `"계좌번호를 입력하세요."` |
| 3단계 | 중복된 계좌번호 | `"이미 등록된 계좌번호입니다: {acctNo}"` |

---

## 주요 특징

- ✅ **검증 우선**: 데이터베이스 작업 전에 모든 검증 수행
- ✅ **원자성**: 성공하면 Account와 주식 데이터가 함께 저장됨
- ✅ **첫 계좌 자동 기본화**: 첫 번째 등록 계좌가 자동으로 기본 계좌가 됨
- ✅ **Transactional**: 메서드 전체가 하나의 트랜잭션으로 처리됨

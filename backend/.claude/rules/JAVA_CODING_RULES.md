# Backend Java 코딩 규칙

이 문서는 `stock-overview` 백엔드 프로젝트의 Java 코딩 규칙을 정의합니다. 19개의 기존 Java 파일을 분석하여 도출한 패턴과 컨벤션을 정리했습니다.

---

## 1. 패키지 구조

### 구조
```
com.stockoverview/
├── StockOverviewApplication.java          # 메인 애플리케이션
├── config/                                # 설정
│   ├── WebConfig.java
│   └── KiwoomProperties.java
├── controller/                            # REST API 레이어
│   ├── AccountController.java
│   └── GlobalExceptionHandler.java        # 중앙 집중식 예외 처리
├── service/                               # 비즈니스 로직 레이어
│   └── AccountService.java
├── dto/                                   # DTO (공용)
│   ├── MonthlySummaryResponse.java
│   ├── MonthlySummaryDto.java
│   ├── DailyBalanceDto.java
│   └── TransactionDto.java
└── kiwoom/                                # 외부 API 통합
    ├── KiwoomAuthService.java
    ├── KiwoomDailyBalanceClient.java
    ├── KiwoomTransactionsClient.java
    ├── KiwoomApiException.java
    └── dto/                               # Kiwoom API 전용 DTO
        ├── KiwoomTokenRequest.java
        ├── KiwoomTokenResponse.java
        ├── Ka01690Request.java
        ├── Ka01690Response.java
        └── Kt00015Request.java
```

### 규칙
- **dto**: 클라이언트에 응답할 때 사용하는 데이터 객체 및 공용 DTO
- **config**: Spring 설정 클래스 및 properties 클래스
- **controller**: REST API 엔드포인트 (RequestMapping 기준으로 폴더 분류) 및 중앙 집중식 예외 처리
- **service**: 비즈니스 로직 처리
- **kiwoom** (또는 외부 API명): 외부 API와의 통신을 담당
  - Client: API 호출 담당
  - Service: 인증, 토큰 관리 등
  - dto: API 요청/응답 객체 (내부용)

---

## 2. 클래스 네이밍 규칙

| 타입 | 패턴 | 예시 |
|------|------|------|
| Controller | `*Controller` | `AccountController` |
| ExceptionHandler | `*ExceptionHandler` or `*Advice` | `GlobalExceptionHandler` |
| Service | `*Service` | `AccountService`, `KiwoomAuthService` |
| Component | `*Component` | - |
| Client | `*Client` | `KiwoomDailyBalanceClient`, `KiwoomTransactionsClient` |
| Exception | `*Exception` | `KiwoomApiException` |
| DTO (공용) | `*Dto`, `*Response` | `DailyBalanceDto`, `MonthlySummaryResponse`, `TransactionDto` |
| DTO (요청) | `*Request` | `Ka01690Request`, `Kt00015Request` |
| DTO (응답) | `*Response` | `Ka01690Response` |
| Config | `*Config`, `*Properties` | `WebConfig`, `KiwoomProperties` |
| 중첩 클래스 | `*Item` | `DayBalRtItem` |

### 규칙
- 항상 영문 대문자 시작 (CamelCase)
- 클래스의 역할을 명확하게 드러내는 suffix 사용
- DTO는 필요에 따라 `Request`, `Response`, `Dto`로 구분

---

## 3. 어노테이션 사용 가이드

### 3.1 DTO 클래스

```java
@Data                    // Lombok: getter/setter/toString/equals/hashCode 자동 생성
@Builder                 // Lombok: Builder 패턴 자동 생성
public class DailyBalanceDto {

    private String date;

    @JsonProperty("estimated_asset")  // JSON 직렬화 시 snake_case 매핑
    private BigDecimal estimatedAsset;
}
```

**규칙**:
- `@Data`: 항상 포함
- `@Builder`: 생성자 방식의 객체 생성이 필요한 경우 포함
- `@JsonProperty`: API 응답이 snake_case인 경우 매핑 지정

### 3.2 Controller 클래스

```java
@RestController                     // REST API 컨트롤러
@RequestMapping("/api/account")     // 기본 경로 지정
@RequiredArgsConstructor            // Lombok: 생성자 의존성 주입
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/daily-balance")
    public ResponseEntity<List<DailyBalanceDto>> getDailyBalance(...) {
        return ResponseEntity.ok(...);
    }
}
```

**규칙**:
- `@RestController`: 항상 포함
- `@RequestMapping`: 기본 경로 지정
- `@RequiredArgsConstructor`: 모든 final 필드를 매개변수로 하는 생성자 자동 생성
- `@GetMapping`, `@PostMapping` 등: 메서드 레벨에서 지정

### 3.3 Service 클래스

```java
@Service                    // Spring 빈 등록
@RequiredArgsConstructor    // 생성자 의존성 주입
@Slf4j                      // Lombok: logger 자동 주입
public class AccountService {

    private final KiwoomDailyBalanceClient dailyBalanceClient;

    public List<DailyBalanceDto> getDailyBalance(...) {
        // ...
    }
}
```

**규칙**:
- `@Service`: 항상 포함
- `@RequiredArgsConstructor`: 항상 포함 (생성자 주입)
- `@Slf4j`: 로깅이 필요한 경우 포함

### 3.4 Component / Client 클래스

```java
@Component                  // Spring 빈 등록
@RequiredArgsConstructor    // 생성자 의존성 주입
@Slf4j                      // 로깅 필요시 포함
public class KiwoomDailyBalanceClient {
    // ...
}
```

### 3.5 Configuration 클래스

```java
@Configuration              // Spring 설정 클래스
public class WebConfig {

    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
```

### 3.6 ConfigurationProperties 클래스

```java
@Component                                    // Spring 빈 등록
@ConfigurationProperties(prefix = "kiwoom")   // properties 파일 매핑
public class KiwoomProperties {

    private String baseUrl = "https://api.kiwoom.com";
    private String appKey = "";
    private String secretKey = "";

    // getter/setter 명시적 작성 (Lombok 미사용)
}
```

**규칙**:
- `@Component`: 빈 등록용
- `@ConfigurationProperties`: prefix 지정
- getter/setter는 수동으로 작성 (Lombok 미사용)

### 3.7 RestControllerAdvice (중앙 집중식 예외 처리)

```java
@RestControllerAdvice          // 모든 Controller의 예외를 처리
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(KiwoomApiException.class)
    public ResponseEntity<Map<String, String>> handleKiwoomApi(KiwoomApiException e) {
        log.warn("Kiwoom API error: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_GATEWAY)
                .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "키움 API 오류"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("Bad request: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "잘못된 요청"));
    }
}
```

**규칙**:
- `@RestControllerAdvice`: 모든 Controller 예외 처리
- `@ExceptionHandler(ExceptionType.class)`: 특정 예외 타입에 대한 처리
- 각 예외 타입별로 적절한 HTTP 상태 코드 반환
- `log.warn`으로 예외 로깅
- 응답은 `Map<String, String>` 형태로 통일

---

## 4. 아키텍처 레이어 규칙

### 4.1 의존성 흐름
```
Controller
    ↓ (의존)
Service
    ↓ (의존)
Client / Component
```

**규칙**:
- 상위 레이어는 하위 레이어에만 의존
- 하위 레이어에서 상위 레이어로 의존 금지
- 계층 간 통신은 DTO를 통해서만 수행

### 4.2 의존성 주입
```java
@RequiredArgsConstructor
public class AccountService {

    // final로 선언된 모든 필드는 생성자에서 주입됨
    private final KiwoomDailyBalanceClient dailyBalanceClient;
    private final AccountRepository accountRepository;  // 필요시
}
```

**규칙**:
- 생성자 주입만 사용 (Field injection 금지)
- 모든 의존성은 `final`로 선언
- `@RequiredArgsConstructor`로 생성자 자동 생성

---

## 5. 코딩 스타일 가이드

### 5.1 필드 및 메서드 접근 제어

```java
public class AccountService {

    // 상수
    private static final DateTimeFormatter API_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final int MAX_DAYS_PER_REQUEST = 365;

    // 의존성
    private final KiwoomDailyBalanceClient dailyBalanceClient;

    // 공개 메서드
    public List<DailyBalanceDto> getDailyBalance(...) { }

    // private 헬퍼 메서드
    private static DailyBalanceDto toDailyBalanceDto(...) { }
    private static BigDecimal parseDecimal(String s) { }
}
```

**규칙**:
- 모든 필드는 `private` (의존성은 `final`)
- 모든 메서드는 `public` 또는 `private`
- 상수는 `static final` + UPPER_SNAKE_CASE
- 복잡한 로직은 private 메서드로 분리

### 5.2 Builder 패턴 (불변성)

```java
// 생성
DailyBalanceDto dto = DailyBalanceDto.builder()
        .date(date.format(OUTPUT_DATE))
        .estimatedAsset(parseDecimal(dayStkAsst))
        .depositBalance(parseDecimal(dbstBal))
        .profitRate(resp.getTotPrftRt())
        .build();

// 사용
MonthlySummaryResponse response = MonthlySummaryResponse.builder()
        .startDate(startDate.format(OUTPUT_DATE))
        .endDate(endDate.format(OUTPUT_DATE))
        .monthlySummaries(monthlySummaries)
        .build();
```

**규칙**:
- DTO는 Builder 패턴으로 생성
- DTO는 생성 후 수정하지 않음 (불변성)
- 모든 DTO에는 `@Builder` 어노테이션 포함

### 5.3 로깅

```java
@Slf4j
public class AccountService {

    public void someMethod() {
        try {
            Ka01690Response resp = dailyBalanceClient.fetchDailyBalance(qryDt);
            if (resp.getReturnCode() != null && resp.getReturnCode() != 0) {
                log.warn("Ka01690 error for {}: {}", qryDt, resp.getReturnMsg());
            }
        } catch (KiwoomApiException e) {
            log.warn("Skip date {}: {}", qryDt, e.getMessage());
        }
    }
}
```

**규칙**:
- `@Slf4j`로 logger 주입
- `log.debug`: 상세 정보
- `log.warn`: 경고/에러 상황
- `log.error`: 예상치 못한 오류
- 로그 메시지는 매개변수 중심 (문자열 연결 금지)

### 5.4 null 안전성

```java
// null 체크
if (startAsset != null && startAsset.compareTo(BigDecimal.ZERO) > 0) {
    // 처리
}

// 기본값 반환
private static BigDecimal parseDecimal(String s) {
    if (s == null || s.isBlank()) {
        return null;  // 또는 BigDecimal.ZERO
    }
    try {
        return new BigDecimal(s.trim().replace(",", ""));
    } catch (NumberFormatException e) {
        return null;
    }
}
```

**규칙**:
- null 가능성이 있는 모든 값에 대해 체크
- null 체크 후 안전하게 처리
- 예외 처리(try-catch) 적절히 사용

### 5.5 Stream API 활용

```java
// grouping
var byMonth = daily.stream()
        .collect(Collectors.groupingBy(d -> d.getDate().substring(0, 7)));

// 정렬
monthlySummaries.sort((a, b) -> a.getYearMonth().compareTo(b.getYearMonth()));

// 필터링 및 변환
List<String> codes = stocks.stream()
        .filter(s -> s.getPrice() > 100)
        .map(Stock::getCode)
        .collect(Collectors.toList());
```

**규칙**:
- 컬렉션 조작에 Stream API 적극 활용
- lambda 표현식 사용
- 복잡한 로직은 명확하게 주석 처리

### 5.6 ObjectMapper 사용 (유연한 JSON 처리)

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class KiwoomTransactionsClient {

    private final ObjectMapper objectMapper = new ObjectMapper();

    @SuppressWarnings("unchecked")
    public List<Map<String, Object>> fetchTransactions(String stDt, String edDt) {
        String raw = restTemplate.postForObject(url, entity, String.class);

        if (raw == null || raw.isBlank()) {
            return List.of();
        }

        try {
            Map<String, Object> map = objectMapper.readValue(raw, new TypeReference<>() {});
            Object code = map.get("return_code");
            if (code != null && !Integer.valueOf(0).equals(code)) {
                log.warn("API error: {}", map.get("return_msg"));
                return List.of();
            }

            // 여러 가능한 필드명 확인
            Object list = map.get("list");
            if (list instanceof List) {
                return (List<Map<String, Object>>) list;
            }

            Object items = map.get("items");
            if (items instanceof List) {
                return (List<Map<String, Object>>) items;
            }

            return List.of();
        } catch (Exception e) {
            log.warn("Failed to parse response: {}", e.getMessage());
            return List.of();
        }
    }
}
```

**규칙**:
- API 응답 구조가 불확실할 때 ObjectMapper 사용
- `String.class`로 원본 JSON 수신 후 유연하게 파싱
- `TypeReference<>()`로 Map 타입으로 변환
- 여러 가능한 필드명 확인 (`list`, `items` 등)
- JSON 파싱 예외는 catch하고 빈 리스트 반환

---

## 6. DTO 작성 규칙

### 6.1 공용 Response DTO

```java
@Data
@Builder
public class MonthlySummaryResponse {

    @JsonProperty("start_date")
    private String startDate;

    @JsonProperty("end_date")
    private String endDate;

    @JsonProperty("start_asset")
    private BigDecimal startAsset;

    @JsonProperty("monthly_summaries")
    private List<MonthlySummaryDto> monthlySummaries;
}
```

**규칙**:
- `@Data`, `@Builder` 필수
- 필드명은 camelCase
- JSON 응답이 snake_case인 경우 `@JsonProperty` 지정
- BigDecimal 사용 (금액/비율용)
- String 사용 (날짜: ISO 포맷)

### 6.2 내부 API DTO (Request)

```java
@Data
@Builder
public class Ka01690Request {

    @JsonProperty("qry_dt")
    private String qryDt;  // YYYYMMDD
}
```

**규칙**:
- 외부 API 요청 형식에 맞춘 필드명
- `@JsonProperty`로 매핑
- 필드명이 이미 snake_case인 경우도 명시

### 6.3 내부 API DTO (Response)

```java
@Data
public class Ka01690Response {

    @JsonProperty("return_code")
    private Integer returnCode;

    @JsonProperty("return_msg")
    private String returnMsg;

    private String dt;  // 일자

    @JsonProperty("tot_evlt_amt")
    private String totEvltAmt;  // 총 평가금액

    @JsonProperty("day_bal_rt")
    private List<DayBalRtItem> dayBalRt;

    // 중첩 클래스
    @Data
    public static class DayBalRtItem {
        @JsonProperty("cur_prc")
        private String curPrc;

        @JsonProperty("stk_cd")
        private String stkCd;
    }
}
```

**규칙**:
- `@Data` 만 사용 (Builder는 선택)
- 응답 객체이므로 setter가 필요
- 중첩 클래스는 `static` + `@Data` 사용
- 필드명은 API 응답 형식 따름

### 6.4 단순 DTO (내부용)

```java
@Data
@Builder
public class TransactionDto {

    private String date;       // yyyy-MM-dd
    private String type;       // 구분: 입금, 출금, 매수, 매도 등
    private BigDecimal amount; // 금액
    private String remark;     // 비고
}
```

**규칙**:
- `@Data`, `@Builder` 포함
- API 응답이 없는 경우 `@JsonProperty` 미사용
- 필드명은 필요에 따라 camelCase 또는 snake_case

---

## 7. API 클라이언트 작성 규칙

### 7.1 기본 구조

```java
@Component
@RequiredArgsConstructor
@Slf4j
public class KiwoomDailyBalanceClient {

    private static final String TR_PATH = "/api/dostk/acnt";
    private static final String API_ID = "ka01690";

    private final KiwoomProperties properties;
    private final RestTemplate restTemplate;
    private final KiwoomAuthService authService;

    public Ka01690Response fetchDailyBalance(String qryDt) {
        // 구현
    }
}
```

**규칙**:
- `@Component` + `@RequiredArgsConstructor` + `@Slf4j` 포함
- API 경로는 `static final` 상수로 정의
- 의존성 주입으로 필요한 서비스 받음

### 7.2 HTTP 요청 구현 (타입 안전 응답)

```java
public Ka01690Response fetchDailyBalance(String qryDt) {
    String token = authService.getAccessToken();
    String url = properties.getBaseUrl() + TR_PATH;

    // 헤더 설정
    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("Authorization", "Bearer " + token);
    headers.set("api-id", API_ID);

    // 요청 바디 생성
    Ka01690Request body = Ka01690Request.builder()
            .qryDt(qryDt)
            .build();

    // HTTP 요청
    HttpEntity<Ka01690Request> entity = new HttpEntity<>(body, headers);
    Ka01690Response response = restTemplate.postForObject(url, entity, Ka01690Response.class);

    // null 체크
    if (response == null) {
        throw new KiwoomApiException("Empty response for date " + qryDt);
    }

    return response;
}
```

**규칙**:
- Bearer token 인증 사용
- `HttpHeaders` 명시적 설정
- 요청/응답 DTO로 Builder 패턴 사용
- null 응답에 대해 예외 발생
- 예외는 커스텀 RuntimeException 사용

### 7.3 HTTP 요청 구현 (유연한 응답 처리)

```java
@SuppressWarnings("unchecked")
public List<Map<String, Object>> fetchTransactions(String stDt, String edDt) {
    String token = authService.getAccessToken();
    String url = properties.getBaseUrl() + TR_PATH;

    HttpHeaders headers = new HttpHeaders();
    headers.setContentType(MediaType.APPLICATION_JSON);
    headers.set("Authorization", "Bearer " + token);
    headers.set("api-id", API_ID);

    Kt00015Request body = Kt00015Request.builder()
            .stDt(stDt)
            .edDt(edDt)
            .build();

    HttpEntity<Kt00015Request> entity = new HttpEntity<>(body, headers);
    String raw = restTemplate.postForObject(url, entity, String.class);

    if (raw == null || raw.isBlank()) {
        return List.of();
    }

    try {
        Map<String, Object> map = objectMapper.readValue(raw, new TypeReference<>() {});
        Object code = map.get("return_code");
        if (code != null && !Integer.valueOf(0).equals(code)) {
            log.warn("API error: {}", map.get("return_msg"));
            return List.of();
        }

        Object list = map.get("list");
        if (list instanceof List) {
            return (List<Map<String, Object>>) list;
        }
        return List.of();
    } catch (Exception e) {
        log.warn("Failed to parse response: {}", e.getMessage());
        return List.of();
    }
}
```

**규칙**:
- 응답 구조가 불확실할 때 `String.class`로 수신
- ObjectMapper 사용해 유연하게 파싱
- 여러 가능한 필드명 확인 후 처리
- 오류 발생 시 빈 리스트 반환

---

## 8. 예외 처리 규칙

### 8.1 커스텀 Exception 정의

```java
public class KiwoomApiException extends RuntimeException {

    public KiwoomApiException(String message) {
        super(message);
    }

    public KiwoomApiException(String message, Throwable cause) {
        super(message, cause);
    }
}
```

**규칙**:
- `RuntimeException` 상속 (checked exception 사용 금지)
- 메시지와 원인(cause)을 받을 수 있는 생성자 제공
- 클래스명은 `*Exception`

### 8.2 Service에서의 예외 처리

```java
public List<DailyBalanceDto> getDailyBalance(String acctNo, LocalDate startDate, LocalDate endDate) {

    List<DailyBalanceDto> result = new ArrayList<>();
    LocalDate current = startDate;

    while (!current.isAfter(endDate)) {
        try {
            Ka01690Response resp = dailyBalanceClient.fetchDailyBalance(qryDt);
            if (resp.getReturnCode() != null && resp.getReturnCode() != 0) {
                log.warn("Ka01690 error for {}: {}", qryDt, resp.getReturnMsg());
            } else {
                result.add(toDailyBalanceDto(current, resp));
            }
        } catch (KiwoomApiException e) {
            log.warn("Skip date {}: {}", qryDt, e.getMessage());
            // 계속 진행
        }
        current = current.plusDays(1);
    }

    return result;
}
```

**규칙**:
- 예외 발생 시 `log.warn` 기록
- 복구 가능한 예외는 catch 후 계속 진행
- 예외 메시지에 컨텍스트(날짜, ID 등) 포함
- 예외를 throw 할 때도 명확한 메시지 제공

### 8.3 GlobalExceptionHandler에서의 예외 처리

```java
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(KiwoomApiException.class)
    public ResponseEntity<Map<String, String>> handleKiwoomApi(KiwoomApiException e) {
        log.warn("Kiwoom API error: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_GATEWAY)
                .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "키움 API 오류"));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("Bad request: {}", e.getMessage());
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage() != null ? e.getMessage() : "잘못된 요청"));
    }
}
```

**규칙**:
- 중앙 집중식 예외 처리를 위해 `@RestControllerAdvice` 사용
- 예외 타입별로 `@ExceptionHandler` 메서드 정의
- 각 예외에 적절한 HTTP 상태 코드 지정
- 응답은 `Map<String, String>` 형태로 통일
- null이 아닌 기본 메시지 제공

---

## 9. 설정 관리 규칙

### 9.1 ConfigurationProperties 클래스

```java
@Component
@ConfigurationProperties(prefix = "kiwoom")
public class KiwoomProperties {

    private String baseUrl = "https://api.kiwoom.com";
    private String appKey = "";
    private String secretKey = "";

    // getter/setter 명시적 작성
    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getAppKey() {
        return appKey;
    }

    public void setAppKey(String appKey) {
        this.appKey = appKey;
    }
}
```

**규칙**:
- `@Component` + `@ConfigurationProperties(prefix = "...")` 사용
- getter/setter는 수동 작성 (Lombok 미사용)
- 기본값 지정
- properties 파일에서 주입

### 9.2 properties 파일

```properties
kiwoom.base-url=https://api.kiwoom.com
kiwoom.app-key=${KIWOOM_APP_KEY}
kiwoom.secret-key=${KIWOOM_SECRET_KEY}
```

---

## 10. 체크리스트

새로운 클래스를 작성할 때 다음을 확인하세요:

### Controller
- [ ] `@RestController` 포함
- [ ] `@RequestMapping` 포함
- [ ] `@RequiredArgsConstructor` 포함
- [ ] 메서드는 `ResponseEntity<T>` 반환
- [ ] 메서드는 `@GetMapping` 또는 `@PostMapping` 지정

### ExceptionHandler
- [ ] `@RestControllerAdvice` 포함
- [ ] `@Slf4j` 포함
- [ ] `@ExceptionHandler(ExceptionType.class)` 각 예외별로 정의
- [ ] 적절한 HTTP 상태 코드 반환
- [ ] 응답은 `Map<String, String>` 형태

### Service
- [ ] `@Service` 포함
- [ ] `@RequiredArgsConstructor` 포함
- [ ] `@Slf4j` 포함
- [ ] 모든 의존성은 `final`로 선언
- [ ] 복잡한 로직은 private 메서드로 분리

### DTO
- [ ] `@Data` + `@Builder` 포함
- [ ] 필드는 private
- [ ] snake_case API에는 `@JsonProperty` 지정
- [ ] Builder로만 생성

### Client
- [ ] `@Component` 포함
- [ ] `@RequiredArgsConstructor` 포함
- [ ] `@Slf4j` 포함
- [ ] 상수는 `static final`로 정의
- [ ] null 응답 체크 및 예외 발생
- [ ] 유연한 응답 처리 필요 시 ObjectMapper 사용

### Exception
- [ ] `RuntimeException` 상속
- [ ] 메시지와 cause를 받는 생성자 제공
- [ ] 예외 발생 시 `log.warn` 기록

---

## 참고

- **프로젝트 Java 버전**: Java 11+
- **Spring Boot**: 2.7+
- **Lombok**: 1.18+
- **Jackson**: 2.13+
- **총 파일 수**: 19개

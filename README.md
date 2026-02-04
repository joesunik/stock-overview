# 키움 계좌 자산 변동 웹 앱

키움 REST API를 통해 특정 계좌의 기간별 월별 자산 변동 차트와 입출금/거래 내역을 조회하는 웹 앱입니다.

## 구성

- **backend**: Spring Boot (Java 17). 키움 OAuth·TR 호출, 월별 집계, 거래 내역 API 제공.
- **frontend**: Vite + React. 기간 선택, 월별 자산 차트(Recharts), 거래 내역 테이블.

## 사전 요구사항

- JDK 17+
- Node.js 18+
- **MySQL** (localhost, root/root 권장. 스키마 `stock_overview`는 앱 기동 시 자동 생성 가능)
- 키움 REST API 앱키·시크릿키 (키움 Open API 가입 후 발급)

## 환경 변수

### Backend

`backend` 실행 전 다음 환경 변수를 설정하세요.

| 변수 | 설명 |
|------|------|
| `KIWOOM_APP_KEY` | 키움 REST API 앱키 |
| `KIWOOM_SECRET_KEY` | 키움 REST API 시크릿키 |
| `KIWOOM_BASE_URL` | (선택) API 도메인. 기본: `https://api.kiwoom.com` |
| `DB_USERNAME` | (선택) MySQL 사용자. 기본: `root` |
| `DB_PASSWORD` | (선택) MySQL 비밀번호. 기본: `root` |

### Frontend

`frontend/.env` 또는 `.env.local`:

```
VITE_API_BASE_URL=http://localhost:8080
```

## 실행 방법

### Backend

```bash
cd backend
./gradlew bootRun
```

또는 환경 변수와 함께:

```bash
cd backend
export KIWOOM_APP_KEY=your_app_key
export KIWOOM_SECRET_KEY=your_secret_key
./gradlew bootRun
```

기본 포트: **8080**

### Frontend

```bash
cd frontend
npm install
npm run dev
```

기본 주소: **http://localhost:5173**

브라우저에서 `http://localhost:5173` 접속 후, **계좌 추가**로 계좌번호를 등록하면 2015년부터 오늘까지 자산 데이터가 키움 API로 수집·DB에 저장됩니다. **계좌 선택** 후 기간을 정하고 **조회**하면 DB에서 월별 자산 변동 차트와 누적 수익률이 표시됩니다. **새로고침** 버튼으로 최근 데이터를 이어서 수집할 수 있습니다.

## API (Backend → Frontend)

| 용도 | 메서드 | 경로 | 설명 |
|------|--------|------|------|
| 계좌 목록 | GET | `/api/accounts` | 계좌 목록 (id, acct_no, is_default) |
| 계좌 추가 | POST | `/api/accounts` | Body `{ "acctNo": "..." }`. 2015~오늘 연별 수집 후 DB 저장 |
| 기본 계좌 설정 | PATCH | `/api/accounts/{id}/default` | 해당 계좌를 기본으로 설정 |
| 새로고침 | POST | `/api/account/refresh` | Query `acctNo`. 갭 또는 전 구간 수집 후 DB 저장 |
| 월별 집계 | GET | `/api/account/monthly-summary` | DB에서 일별 조회 후 월별 집계 (startDate, endDate, acctNo) |
| 일별 자산 | GET | `/api/account/daily-balance` | DB에서만 조회 |
| 거래/입출금 | GET | `/api/account/transactions` | 키움 kt00015 호출 (startDate, endDate, acctNo) |

## DB (MySQL)

- 스키마: `stock_overview` (URL에 `createDatabaseIfNotExist=true`로 자동 생성 가능)
- 테이블: `account` (계좌), `daily_balance` (계좌별 일별 자산). JPA `ddl-auto: update`로 테이블 생성/수정.

## 참고

- 계좌 추가 시 2015년~현재까지 **연별** 키움 **ka01690** 호출로 데이터를 수집하므로 완료까지 시간이 걸릴 수 있습니다.
- 새로고침: 데이터가 없으면 2015~오늘, 있으면 최근 저장일 다음날~오늘만 수집합니다.
- 월별/일별 조회는 **DB만** 사용하며, 키움은 호출하지 않습니다. 거래 내역만 키움 kt00015를 사용합니다.
- 키움 앱키·시크릿키는 반드시 서버(백엔드) 환경 변수로만 설정하고, 프론트엔드나 코드에 노출하지 마세요.

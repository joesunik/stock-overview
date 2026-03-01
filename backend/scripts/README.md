# Oracle SSO 스키마 생성

Oracle이 Docker로 실행 중일 때(FREEPDB1, sys/1234), 아래 중 하나로 실행하세요.

## 1. docker exec로 한 번에 실행

컨테이너 이름을 확인한 뒤(`docker ps`):

```bash
docker exec -i <Oracle컨테이너이름> sqlplus -s sys/1234@//localhost:1521/FREEPDB1 as sysdba < oracle-create-sso-user.sql
```

예:

```bash
cd backend/scripts
docker exec -i $(docker ps -q -f ancestor=container-registry.oracle.com/database/free) sqlplus -s sys/1234@//localhost:1521/FREEPDB1 as sysdba < oracle-create-sso-user.sql
```

또는 컨테이너 이름을 알고 있으면:

```bash
docker exec -i oracle-db sqlplus -s sys/1234@//localhost:1521/FREEPDB1 as sysdba < oracle-create-sso-user.sql
```

## 2. 컨테이너 안에서 실행

파일을 컨테이너로 복사한 뒤:

```bash
docker cp oracle-create-sso-user.sql <컨테이너이름>:/tmp/
docker exec -it <컨테이너이름> sqlplus sys/1234@//localhost:1521/FREEPDB1 as sysdba @/tmp/oracle-create-sso-user.sql
```

## 생성 결과

- **스키마(유저)**: `sso` / 비밀번호 `sso`
- **테이블**: 앱 기동 시 JPA `ddl-auto: update`로 SSO 스키마에 `SSO_USER`, `OAUTH2_CLIENT` 등 생성됨

`application.yml` 기본값이 이미 `ORACLE_USER: sso`, `ORACLE_PASSWORD: sso`로 되어 있으므로, 위 스크립트 실행 후 앱만 기동하면 됩니다.

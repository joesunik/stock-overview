-- Oracle Docker (FREEPDB1)에서 SSO 스키마(유저) 생성
-- 실행: docker exec -i <컨테이너이름> sqlplus -s sys/1234@//localhost:1521/FREEPDB1 as sysdba @-
-- 또는 컨테이너 안에서: sqlplus sys/1234@//localhost:1521/FREEPDB1 as sysdba @/path/to/this/file.sql

ALTER SESSION SET CONTAINER = FREEPDB1;

CREATE USER sso IDENTIFIED BY sso
  DEFAULT TABLESPACE USERS
  TEMPORARY TABLESPACE TEMP;

GRANT CREATE SESSION TO sso;
GRANT CONNECT TO sso;
GRANT RESOURCE TO sso;
GRANT CREATE TABLE TO sso;
GRANT CREATE SEQUENCE TO sso;
ALTER USER sso QUOTA UNLIMITED ON USERS;

EXIT;

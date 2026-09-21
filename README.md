# Sporty

공공 체육시설 정보와 AI를 활용하여 사용자의 운동 활동을 지원하는 서비스입니다.

## Tech Stack

### Frontend
- React
- Create React App

### Backend
- Java
- Spring Boot
- Spring Data JPA
- Hibernate
- MariaDB

## Project Structure

```text
.
├── frontend/           # React frontend
├── backend/            # Spring Boot backend
├── docs/               # 프로젝트 문서 및 산출물
│   └── deliverables/
└── README.md
```

## Local Setup

### 1. Repository Clone

```bash
git clone <repository-url>
cd <repository-name>
```

### 2. Database Setup

로컬 환경에 MariaDB가 설치되어 있어야 합니다.

MariaDB에서 프로젝트용 데이터베이스를 생성합니다.

```sql
CREATE DATABASE sporty;
```

테이블은 Spring Boot 실행 시 Hibernate가 Entity를 기준으로 생성하므로 직접 생성할 필요가 없습니다.

개발 환경에서는 다음 설정을 사용합니다.

```yaml
spring:
  jpa:
    hibernate:
      ddl-auto: update
```

### 3. Backend Configuration

각자의 로컬 DB 접속 정보를 설정합니다.

예시:

```text
DB_URL=jdbc:mariadb://localhost:3306/sporty
DB_USERNAME=root
DB_PASSWORD=your_password
```

`application.yml`에서는 다음과 같이 환경변수를 사용합니다.

```yaml
spring:
  datasource:
    url: ${DB_URL}
    username: ${DB_USERNAME}
    password: ${DB_PASSWORD}

  jpa:
    hibernate:
      ddl-auto: update
```

DB 계정, 비밀번호 등 개인별 설정은 Git에 commit하지 않습니다.

### 4. Backend Run

```bash
cd backend
```

Windows:

```bash
gradlew.bat bootRun
```

macOS / Linux:

```bash
./gradlew bootRun
```

Spring Boot가 실행되면 Hibernate가 Entity를 기준으로 필요한 테이블을 생성합니다.

### 5. Frontend Setup

```bash
cd frontend
npm install
npm start
```

`node_modules`는 Git으로 관리하지 않습니다.

`npm install`을 실행하면 `package.json`과 `package-lock.json`을 기준으로 필요한 패키지가 설치됩니다.

## Git Convention

### Branch

기능 개발은 별도의 branch에서 진행합니다.

```bash
git checkout -b feature/<issue-number>-<feature-name>
```

예시:

```bash
git checkout -b feature/12-create-match-api
```

작업 완료 후 Pull Request를 생성하여 `main` branch에 병합합니다.

### Commit Message

Commit message는 다음 형식을 권장합니다.

```text
type: title

body
```

예시:

```text
feat: add match creation API

Implement API for creating exercise matches.
```

주요 type:

- `feat`: 새로운 기능
- `fix`: 버그 수정
- `docs`: 문서 수정
- `test`: 테스트 추가 또는 수정
- `refactor`: 리팩터링
- `chore`: 설정 및 기타 작업

## Documents

프로젝트 관련 문서는 `docs/` 디렉토리에서 관리합니다.

```text
docs/
├── deliverables/
└── ...
```

요구사항 명세서, API 명세, 테스트 케이스 등의 프로젝트 산출물을 이곳에서 관리합니다.

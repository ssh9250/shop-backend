# CLAUDE.md

이 파일은 Claude Code (claude.ai/code)가 이 저장소의 코드를 작업할 때 참고할 수 있는 가이드를 제공합니다.

## 프로젝트 개요
Java 17로 구축된 Spring Boot 3.3.5 애플리케이션입니다. 음악/악기 중심의 이커머스 플랫폼으로 게시글/댓글 기능, 회원 관리, 악보 변환을 위한 AI 통합 기능을 제공합니다.

## 빌드 및 개발 명령어

### 프로젝트 빌드
```bash
./gradlew build
```

### 애플리케이션 실행
```bash
./gradlew bootRun
```

### 테스트 실행
```bash
./gradlew test
```

### 특정 테스트 클래스 실행
```bash
./gradlew test --tests "com.study.shop.domain.post.controller.PostControllerTest"
```

### QueryDSL 클래스 생성
QueryDSL Q-클래스는 컴파일 시 자동으로 생성됩니다. 생성된 파일은 `$buildDir/generated/querydsl`에 위치합니다.

### API 문서
- 애플리케이션 실행 시 `/swagger-ui.html`에서 Swagger UI를 사용할 수 있습니다
- OpenAPI 3 문서는 `/v3/api-docs`에서 확인할 수 있습니다

## 아키텍처

### 패키지 구조
애플리케이션은 도메인 주도 설계(DDD) 원칙을 따릅니다:

- `domain/` - 핵심 비즈니스 도메인 (post, comment, member, instrument, auth)
  - 각 도메인은 다음을 포함: `dto/`, `entity/`, `repository/`, `controller/`, `service/`, `exception/`
- `global/` - 공통 관심사
  - `config/` - 설정 클래스 (QueryDSL, JPA Auditing, Swagger, Web)
  - `security/` - 보안 설정 및 인증
  - `exception/` - 전역 예외 처리
  - `response/` - 공통 응답 DTO
- `infrastructure/` - 외부 통합
  - `ai/` - 악보 변환 서비스
  - `external/` - 스토리지 및 기타 외부 서비스
- `api/` - API 레이어 (member, market)
- `support/` - 유틸리티 클래스, 상수, 열거형

### 주요 기술 스택
- **Spring Boot 3.3.5** (Spring Web, JPA, Security, Validation 포함)
- **QueryDSL 5.0.0** (타입 안전 쿼리를 위한 Q-클래스 자동 생성)
- **Lombok** (보일러플레이트 코드 감소)
- **H2 Database** (개발/테스트용)
- **JUnit 5** (Spring Boot Test와 함께 테스트용)
- **SpringDoc OpenAPI** (API 문서화)
- **Spring Security** (인증/인가)

### 데이터베이스 설정
- Hibernate와 함께 Spring Data JPA 사용
- 개발용 H2 인메모리 데이터베이스
- JPA Auditing은 자동 타임스탬프 관리를 위해 구성됨
- 개발용 SQL 포맷팅 및 로깅 설정

### 테스트 전략
- 통합 테스트를 위해 `@AutoConfigureMockMvc`와 함께 `@SpringBootTest` 사용
- 테스트 헬퍼는 `src/test/java/com/study/shop/testutil/`에서 사용 가능
- 컨트롤러 테스트는 HTTP 요청 시뮬레이션을 위해 MockMvc 사용

### 설정 프로파일
- `application.yml` - 기본 설정
- `application-dev.yml` - 개발 프로파일
- `application-prod.yml` - 운영 프로파일

## 개발 참고사항

### QueryDSL 사용법
- Q-클래스는 `$buildDir/generated/querydsl`에 자동 생성됩니다
- 리포지토리 레이어에서 복잡한 쿼리를 위해 QueryDSL을 사용할 수 있습니다
- 생성된 소스는 메인 소스 세트에 포함됩니다

### AI 통합
애플리케이션은 악보 변환 기능을 포함합니다:
- `MusicScoreConverter` - 핵심 변환 로직
- `MusicScoreClient` - 외부 AI 서비스 통합

### 보안
- Spring Security는 커스텀 인증으로 구성되어 있습니다
- 보안 설정은 `global/security/config/`에 있습니다
- 인증 로직은 `global/security/auth/`에 있습니다

### 로깅
- Root 레벨: INFO
- Application 레벨 (`com.study.cruisin`): DEBUG
- Hibernate SQL: WARN
- Spring framework: WARN
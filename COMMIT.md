token provider validate token 검증 로직 재확인
로그아웃 시 refresh token 삭제
github actions에 커버리지 리포트 생성

ok) Refresh Token Rotation 로직 상세 이해

1. QueryDSL 동적 검색 + 페이징
   면접 포인트: "왜 JPQL 안 쓰고 QueryDSL?"
   → 타입 안전성, 컴파일 타임 오류 감지, 동적 조건 조합
   → count 쿼리 분리로 성능 최적화까지 설명 가능
2. Cursor 기반 페이징 (상품/피드)
   면접 포인트: "Offset과 Cursor의 차이?"
   → 대용량 데이터에서 offset의 성능 저하 (LIMIT 10000, 20)
   → 복합 커서 (createdAt + id) 로 안정성 확보
   → 실무에서 무한스크롤은 거의 Cursor 사용
3. Redis 활용 다양화
   현재: Refresh Token + Blacklist
   추가: 인기글 캐싱, 조회수 증가 (Write-behind)
   면접 포인트: "Redis를 어떤 문제를 해결하기 위해 썼나요?"
   → DB 부하 분산, 실시간 카운터, TTL 기반 캐시 전략


// -Dsun.java2d.metal=false

security config filter bean 중복 생성 문제 해결 issue에 기록

마지막 커밋 이후의 변경사항에 대한 작업 내용을 이전 커밋 메시지들을 참고하여 가능한 간단하게 COMMIT.md의 맨 마지막에 작성해주시고, 커밋은 하지마세요.
현재까지 진행된 내용을 project_knowledge.md에 반영하여 업데이트해주세요.


feat : 새로운 기능 추가
fix : 버그 수정
docs : 문서 수정
style : 코드 포맷팅, 세미콜론 누락, 코드 변경이 없는 경우
refactor : 코드 리펙토링
test : 테스트 코드, 리펙토링 테스트 코드 추가
chore : 빌드 업무 수정, 패키지 매니저 수정

---

Feat: Soft Delete 전체 도메인 확장, Swagger JWT 설정 완성, 목록 캐싱 롤백
- Post/Comment/Category: @SQLDelete/@SQLRestriction 추가, BaseTimeEntity 상속
- Order: BaseTimeEntity 상속 추가 (deleted_at 필드 매핑 누락 수정)
- Post/Comment/Order/Category: @Builder.Default 누락 수정 (hidden, viewCount, deleted 등)
- SwaggerConfig: SecurityScheme(Bearer JWT) Components 등록 완성
- SecurityConfig: /api/auth/signup, /init/** permitAll 추가
- PostService: @Cacheable/@CacheEvict 주석 처리 (Page<T> 직렬화 문제로 목록 캐싱 롤백)
- InitController: 개발용 데이터 초기화 엔드포인트 추가
- docker-compose.yml: MySQL utf8mb4 charset 설정 추가
- build.gradle: springdoc-openapi 의존성 중복 제거
- logging: 환경별 로그 레벨 정리 (application.yml, application-local.yml)

Feat: JaCoCo 설정 추가, MemberControllerTest 작성, SecurityConfig 예외 처리 보완
- build.gradle: jacoco 플러그인 추가, jacocoTestReport/jacocoTestCoverageVerification 설정 (classDirectories 필터 동기화, 70% 기준)
- build.gradle: querydslDir 단일 따옴표 → 이중 따옴표 버그 수정 ($buildDir 미보간 문제)
- MemberControllerTest: GET /me, PATCH /profile, PATCH /password, DELETE 총 13개 케이스 작성
- SecurityConfig: authenticationEntryPoint/accessDeniedHandler → SecurityResponseUtil 연동
- application-test.yml: H2 MODE=MySQL, MySQL8Dialect, file.upload-dir 추가

Test: RefreshTokenServiceTest 작성, Gradle 업그레이드, 환경 설정 정리
- RefreshTokenServiceTest: JaCoCo 커버리지 미달 구간 7개 케이스 추가 (validateRefreshToken null/불일치, rotateRefreshToken not found/mismatch, hasRefreshToken)
- build.gradle: spring-security-test 의존성 제거, querydslDir sourceSets 블록 제거
- gradle-wrapper: 8.10.2 → 9.4.0 업그레이드
- application-dev.yml: H2 tcp → mem 전환, jwt.secret/file.upload-dir 추가
- application.yml: active profile local → dev 변경


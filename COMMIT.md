ok) custom user detail 엔티티 대신 필수 정보만 + service 토스 객체도 수정
ok) tokenDto, jwtProperties 빈 파일 해결
x) authentication filter에 tokenProvider, redisTemplate 관련해서 filter config에서 빈 자동화
token provider validate token 검증 로직 재확인
authservice signup 로직 강화하기 (중복체크, 유효체크 api 추가 등/또는 memberservice에서 해야 할 작업인지 확인)
swaggerConfig에서 jwt 관련 설정 마무리
로그아웃 시 refresh token 삭제

Refresh Token Rotation 로직 상세 이해

security config filter bean 중복 생성 문제 해결 issue에 기록


feat : 새로운 기능 추가
fix : 버그 수정
docs : 문서 수정
style : 코드 포맷팅, 세미콜론 누락, 코드 변경이 없는 경우
refactor : 코드 리펙토링
test : 테스트 코드, 리펙토링 테스트 코드 추가
chore : 빌드 업무 수정, 패키지 매니저 수정

---

feat: 로그아웃 API 추가 (진행중)
- AuthController에 로그아웃 엔드포인트 추가
- AuthService에 로그아웃 로직 구현 (미완성 - Access Token 블랙리스트 처리 필요)
- Refresh Token 삭제 기능 포함

feat: Redis 테스트 컨트롤러 추가
- RedisTestController 생성 및 기본 테스트 엔드포인트 구현
- /api/test/redis 엔드포인트로 Redis 연결 테스트 가능

refactor: 설정 파일 프로파일 정리
- application.yml에 active profile 설정 추가 (local)
- application-dev.yml, application-local.yml에 on-profile 명시
- application-local.yml 전체 설정 추가 (datasource, jpa, redis, logging 등)

refactor: 불필요한 클래스 및 코드 정리
- RefreshTokenRotationPolicy.java 삭제 (주석만 있던 정책 파일)
- MemberRepository.findByEmailWithRoles 쿼리 주석 처리

fix: JwtTokenProvider 오타 수정
- @Value 어노테이션의 refresh-expiration-ms 속성명 오타 수정

style: 코드 개선
- JwtAuthenticationFilter, JwtTokenProvider 주석 추가
- SecurityConfig 필터 순서 조정 (JwtAuthenticationFilter -> JwtExceptionFilter 순으로 변경)
- InstrumentController CustomUserDetails 사용법 변경 (getMember().getId() -> getMemberId())
- codeRequest.http 파일 생성 및 테스트 요청 추가


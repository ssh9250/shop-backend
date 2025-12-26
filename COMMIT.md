ok) custom user detail 엔티티 대신 필수 정보만 + service 토스 객체도 수정
ok) tokenDto, jwtProperties 빈 파일 해결
x) authentication filter에 tokenProvider, redisTemplate 관련해서 filter config에서 빈 자동화
token provider validate token 검증 로직 재확인
authservice signup 로직 강화하기 (중복체크, 유효체크 api 추가 등/또는 memberservice에서 해야 할 작업인지 확인)
swaggerConfig에서 jwt 관련 설정 마무리
로그아웃 시 refresh token 삭제

ok) Refresh Token Rotation 로직 상세 이해

security config filter bean 중복 생성 문제 해결 issue에 기록


feat : 새로운 기능 추가
fix : 버그 수정
docs : 문서 수정
style : 코드 포맷팅, 세미콜론 누락, 코드 변경이 없는 경우
refactor : 코드 리펙토링
test : 테스트 코드, 리펙토링 테스트 코드 추가
chore : 빌드 업무 수정, 패키지 매니저 수정

---

feat: 로그아웃 API 구현 완료
- AuthController 로그아웃 엔드포인트 추가 (/api/auth/logout)
- AuthService 로그아웃 로직 구현 (Refresh Token 삭제 + Access Token 블랙리스트 처리)
- JwtTokenProvider에 토큰 만료 시간 조회 메서드 추가 (getExpiration)
- HTTP 테스트 파일 추가 (signup/login/logout 요청)

refactor: Security 설정 및 로그인 플로우 개선
- SecurityConfig에서 기본 logout 설정 제거 (커스텀 로그아웃 API 사용)
- AuthService.login 메서드 리팩토링 (RefreshTokenService 호출 방식 변경)
- CustomUserDetails 사용법 변경 (InstrumentController)

chore: 설정 파일 프로파일 정리
- application.yml에 active profile 설정 (local)
- application-dev.yml, application-local.yml에 on-profile 명시
- application-local.yml 전체 설정 추가

refactor: 불필요한 코드 정리
- RefreshTokenRotationPolicy.java 삭제
- MemberRepository.findByEmailWithRoles 쿼리 주석 처리

fix: JwtTokenProvider 설정 오류 수정
- @Value 어노테이션 refresh-expiration-ms 오타 수정

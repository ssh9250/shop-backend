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

docs: JWT 인증 플로우 문서 추가 (readme.md)
- 로그인 플로우 및 API 요청 시 인증 플로우 정리
- 주요 클래스별 역할 명시

feat: Refresh Token 갱신 API 추가 (/api/auth/refresh)
refactor: CustomUserDetails 엔티티 의존성 제거, 필수 정보만 저장
refactor: Security 예외 핸들러 및 불필요한 클래스 삭제
- JwtAuthenticationEntryPoint, JwtAccessDeniedHandler 제거
- JwtProperties, TokenDto, RefreshToken 빈 파일 삭제

ok) custom user detail 엔티티 대신 필수 정보만 + service 토스 객체도 수정
ok) tokenDto, jwtProperties 빈 파일 해결
x) authentication filter에 tokenProvider, redisTemplate 관련해서 filter config에서 빈 자동화
token provider validate token 검증 로직 재확인
authservice signup 로직 강화하기 (중복체크, 유효체크 api 추가 등/또는 memberservice에서 해야 할 작업인지 확인)
swaggerConfig에서 jwt 관련 설정 마무리
로그아웃 시 refresh token 삭제

ok) Refresh Token Rotation 로직 상세 이해

security config filter bean 중복 생성 문제 해결 issue에 기록

마지막 커밋 이후의 변경사항에 대한 내용을 이전 커밋 메시지들을 참고하여 COMMIT.md의 맨 마지막에 작성해주시고, 커밋은 하지마세요.


feat : 새로운 기능 추가
fix : 버그 수정
docs : 문서 수정
style : 코드 포맷팅, 세미콜론 누락, 코드 변경이 없는 경우
refactor : 코드 리펙토링
test : 테스트 코드, 리펙토링 테스트 코드 추가
chore : 빌드 업무 수정, 패키지 매니저 수정

---

Test: 토큰 재발급 및 로그아웃 API 테스트 코드 추가
- RefreshControllerTest를 RefreshAndLogoutControllerTest로 리네이밍
- IntegrationTestBase 상속으로 테스트 구조 통일
- 토큰 재발급(Refresh) 테스트 케이스 추가
  - 정상 토큰 재발급
  - 유효하지 않은 refreshToken으로 재발급 실패
  - refreshToken 누락 시 재발급 실패
  - 빈 문자열 refreshToken으로 재발급 실패
- 로그아웃(Logout) 테스트 케이스 추가
  - 정상 로그아웃
  - 인증 없이 로그아웃 시도 시 실패
  - 유효하지 않은 토큰으로 로그아웃 시도 시 실패
  - 로그아웃 후 동일 토큰으로 재요청 시 실패 (블랙리스트 검증)
  - 로그아웃 후 refreshToken으로 토큰 재발급 실패
- LoginControllerTest, SignupControllerTest 코드 스타일 정리

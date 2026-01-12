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

test: 회원가입 및 로그인 컨트롤러 테스트 코드 추가
- SignupControllerTest 생성 (회원가입 API 테스트)
- LoginControllerTest 생성 (로그인 API 테스트)
- RefreshControllerTest 생성 (토큰 갱신 API 테스트 준비)
- AuthController에 @Valid 어노테이션 추가 (signup, login 메서드)
- LoginRequestDto에 @Email validation 추가
- MemberControllerTest에 @Transactional 추가 및 불필요한 테스트 코드 제거

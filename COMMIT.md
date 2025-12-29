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

feat: 회원가입 및 프로필 수정 시 이메일/닉네임 중복 체크 기능 추가
- DuplicateEmailException, DuplicateNicknameException 커스텀 예외 추가
- ErrorCode에 DUPLICATE_EMAIL, DUPLICATE_NICKNAME 에러 코드 추가 (HttpStatus.CONFLICT)
- MemberService에 validateDuplicateEmail, validateDuplicateNickname 검증 메서드 추가

refactor: MemberController 코드 정리

refactor: ErrorCode 주석 정리
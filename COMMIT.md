ok) custom user detail 엔티티 대신 필수 정보만 + service 토스 객체도 수정
ok) tokenDto, jwtProperties 빈 파일 해결
x) authentication filter에 tokenProvider, redisTemplate 관련해서 filter config에서 빈 자동화
token provider validate token 검증 로직 재확인
auth service signup 로직 강화하기 (중복체크, 유효체크 api 추가 등/또는 member service에서 해야 할 작업인지 확인)
swaggerConfig에서 jwt 관련 설정 마무리
로그아웃 시 refresh token 삭제

ok) Refresh Token Rotation 로직 상세 이해

security config filter bean 중복 생성 문제 해결 issue에 기록

마지막 커밋 이후의 변경사항에 대한 작업 내용을 이전 커밋 메시지들을 참고하여 가능한 간단하게 COMMIT.md의 맨 마지막에 작성해주시고, 커밋은 하지마세요.


feat : 새로운 기능 추가
fix : 버그 수정
docs : 문서 수정
style : 코드 포맷팅, 세미콜론 누락, 코드 변경이 없는 경우
refactor : 코드 리펙토링
test : 테스트 코드, 리펙토링 테스트 코드 추가
chore : 빌드 업무 수정, 패키지 매니저 수정

---

Feat: Post 파일 첨부 기능 구현 완료
- FileStorageService 구현 (로컬 파일 시스템, UUID 기반 파일명으로 저장/삭제)
- PostController createPost/updatePost를 @RequestPart 기반 멀티파트로 전환
- Post 엔티티에 create(정적 팩토리), update, addPostFile, removePostFile 도메인 메서드 추가
- PostFile 엔티티에 Lombok 및 패키지 프라이빗 assignPost() 추가
- PostService에 파일 업로드/삭제 통합, updatePost/deletePost에 접근 권한 검증 추가
- application.yml에 file.upload-dir 설정 추가
- ISSUE.md에 #011 Order-OrderItem 순환 의존성 해결 분석 기록

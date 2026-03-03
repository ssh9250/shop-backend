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
현재까지 진행된 내용을 project_knowledge.md에 반영하여 업데이트해주세요.


feat : 새로운 기능 추가
fix : 버그 수정
docs : 문서 수정
style : 코드 포맷팅, 세미콜론 누락, 코드 변경이 없는 경우
refactor : 코드 리펙토링
test : 테스트 코드, 리펙토링 테스트 코드 추가
chore : 빌드 업무 수정, 패키지 매니저 수정

---

Feat: Board 관리자 API 구현 및 Post 페이징 적용
- Post, Comment 관련 관리자 로직은 BoardAdminController/Service로 통합
- BoardAdminController에 @PreAuthorize("hasRole('ADMIN')") 적용, 게시글/댓글 조회·강제삭제 구현
- AdminCommentResponseDto 추가 (deleted 필드 포함, 소프트 삭제 상태 노출)
- CommentRepository에 findAllByPostId() 추가 (소프트 삭제 포함 관리자 전용 조회)
- PostRepository에 findAllWithMember() (fetch join, countQuery 분리), findByMemberId() 추가
- PostController/Service getAllPosts()를 Page 기반 페이징으로 전환 (@PageableDefault size=20, createdAt DESC)

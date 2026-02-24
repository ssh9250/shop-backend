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

Feat: Item/Comment 도메인 인증 적용 및 ItemStatus 도입
- Item 엔티티에 ItemStatus 열거형 도입 (available → ON_SALE/RESERVED/SOLD_OUT/HIDDEN), create() 정적 팩토리 및 validateOrderable() 추가
- ItemController/Service에 @AuthenticationPrincipal 적용, getAllItem() 엔드포인트 추가, ON_SALE 상태 검증 추가
- Comment 엔티티에 create() 정적 팩토리 및 assignPost/assignMember 도메인 메서드 추가
- CommentController/Service에 인증 적용, CreateCommentRequestDto에서 writer 필드 제거
- Member 엔티티에 items 연관관계(@OneToMany) 추가
- OrderItem.create()에 validateOrderable() 호출 추가, OrderService.validateOrderAccess() 불필요한 Member 조회 제거
- FileStorageService 오타 수정 (delteFile → deleteFile)
- Relation.md 추가 (엔티티 연관관계 및 N+1 분석 문서화)
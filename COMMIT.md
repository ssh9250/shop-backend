ok) custom user detail 엔티티 대신 필수 정보만 + service 토스 객체도 수정
ok) tokenDto, jwtProperties 빈 파일 해결
x) authentication filter에 tokenProvider, redisTemplate 관련해서 filter config에서 빈 자동화
token provider validate token 검증 로직 재확인
auth service signup 로직 강화하기 (중복체크, 유효체크 api 추가 등/또는 member service에서 해야 할 작업인지 확인)
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

Refactor: Instrument 도메인을 Item으로 리네이밍 및 Order 도메인 구조 개선
- Instrument 도메인을 Item 도메인으로 전면 리네이밍
  - 패키지명 변경: domain/instrument → domain/Item
  - 엔티티 리네이밍: Instrument → Item
  - 컨트롤러 리네이밍: InstrumentController → ItemController
  - DTO 리네이밍: CreateInstrumentRequestDto → CreateItemRequestDto
  - DTO 리네이밍: InstrumentResponseDto → ItemResponseDto
  - DTO 리네이밍: UpdateInstrumentRequestDto → UpdateItemRequestDto
  - 서비스 및 레포지토리 import 경로 수정
- Order 도메인 구조 개선
  - Order 엔티티를 order/entity/ 하위로 이동
  - Order 엔티티에 필드 추가 (orderItems, orderDate, totalPrice, orderStatus, address)
  - OrderItem 엔티티를 order/entity/ 하위로 이동 및 JPA 엔티티로 구현
  - Order-OrderItem 간 양방향 연관관계 설정
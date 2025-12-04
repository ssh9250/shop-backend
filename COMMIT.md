# Commit Message

```
feat: JWT 보안 강화 - Secret 검증 및 예외 처리 구현

## 주요 변경사항

### JwtTokenProvider (보안 강화)
- Secret key 길이 검증 로직 추가
  - HS256 알고리즘 요구사항에 따라 최소 32바이트 검증
  - 애플리케이션 시작 시 잘못된 secret 조기 감지
  - 명확한 에러 메시지로 현재 길이 표시
- UTF-8 인코딩 명시
  - `secret.getBytes()` → `secret.getBytes(StandardCharsets.UTF_8)`
  - 플랫폼 독립적인 인코딩 보장
  - 모든 환경에서 동일한 키 생성
- 코드 포맷팅 개선 (catch 블록 정리)

### JwtExceptionFilter (신규 구현)
- OncePerRequestFilter를 상속한 JWT 예외 처리 필터 구현
- JwtException 발생 시 적절한 에러 응답 반환
  - 401 Unauthorized 상태 코드
  - JSON 형식의 에러 메시지
  - 클라이언트가 인증 실패 원인 파악 가능
- SecurityFilterChain에 통합 (JwtAuthenticationFilter 이전에 배치)

### REVIEW.md (신규 문서)
- JWT 인증 메커니즘 종합 보안 리뷰 문서 작성
- 이론적 작동 분석 및 보안 취약점 11개 식별
- 우선순위별 개선 작업 로드맵 제시
  - 🔴 즉시 수정 필요: 4개 (일부 완료)
  - 🟡 조기 개선: 4개
  - 🟢 장기 개선: 4개

## 보안 개선 효과

### 1. Secret Key 검증
```java
byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
if (secretBytes.length < 32) {
    throw new IllegalArgumentException(
        "JWT secret key 의 길이는 최소 32바이트 이상이여야 합니다. 현재 길이 : " + secretBytes.length
    );
}
```
- REVIEW.md에서 지적된 Critical 보안 취약점 해결
- 약한 secret key로 인한 토큰 위변조 방지

### 2. JWT 예외 처리
```java
try {
    filterChain.doFilter(request, response);
} catch (JwtException e) {
    setErrorResponse(response, e.getMessage());
}
```
- REVIEW.md에서 지적된 Critical 보안 취약점 해결
- 명확한 에러 응답으로 클라이언트 디버깅 지원

## 참고
- REVIEW.md의 3.1, 3.2 항목 보안 취약점 해결
- 운영 배포 전 필수 개선 사항 2/4 완료
- 나머지 Critical 이슈: 만료 토큰 처리, 블랙리스트 TTL
```

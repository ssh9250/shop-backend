# JWT 인증 메커니즘 보안 리뷰

## 1. 전체 아키텍처 개요

### 1.1 구현된 컴포넌트
- **JwtTokenProvider**: JWT 토큰 생성, 검증, 파싱
- **JwtAuthenticationFilter**: 요청마다 JWT 검증 및 인증 설정
- **JwtExceptionFilter**: JWT 예외 처리 (미구현)
- **RefreshTokenService**: Refresh Token 생성, 검증, 갱신
- **RefreshTokenRepository**: Redis 기반 Refresh Token 저장소
- **SecurityConfig**: Spring Security 설정

### 1.2 토큰 전략
- **Access Token**: 30분 (1,800,000ms)
- **Refresh Token**: 14일 (1,209,600,000ms)
- **알고리즘**: HS256 (HMAC-SHA256)
- **저장소**: Redis (Refresh Token, 블랙리스트)
- **세션 정책**: STATELESS

---

## 2. 이론적 작동 분석

### 2.1 정상 플로우

#### 로그인
1. 사용자 인증 성공
2. Access Token + Refresh Token 생성
3. Refresh Token을 Redis에 저장 (email을 키로 사용)
4. 클라이언트에 두 토큰 반환

#### API 요청
1. Authorization 헤더에 \`Bearer {accessToken}\` 포함
2. JwtAuthenticationFilter에서 토큰 추출
3. 블랙리스트 확인 (Redis)
4. 토큰 유효성 검증
5. 유효하면 SecurityContext에 인증 정보 설정
6. 요청 처리

#### 로그아웃
1. Access Token을 블랙리스트에 추가 (Redis)
2. Refresh Token을 Redis에서 삭제

#### Token Refresh
1. 만료된 Access Token + Refresh Token 제공
2. Refresh Token 검증
3. Redis에 저장된 토큰과 일치 확인
4. 새로운 Access Token + Refresh Token 발급 (Rotation)
5. 기존 Refresh Token 삭제, 새 토큰 저장

### 2.2 보안 메커니즘

✅ **Token Rotation**: Refresh Token 사용 시 새로운 토큰으로 교체
✅ **Reuse Detection**: 이전 Refresh Token 재사용 감지 시 토큰 삭제
✅ **Blacklist**: 로그아웃된 토큰을 Redis에 저장하여 재사용 방지
✅ **STATELESS**: 서버 세션 없이 토큰 기반 인증

---

## 3. 보안 취약점 분석

### 🔴 심각 (Critical)

#### 3.1 Secret Key 관리 취약점
**위치**: \`JwtTokenProvider.java:35\`

**문제점**:
- \`String.getBytes()\`는 플랫폼 기본 인코딩 사용 → 환경마다 다른 키 생성 가능
- Secret 길이 검증 없음 (HS256은 최소 256비트 = 32바이트 필요)
- Secret 강도 검증 없음

**권장 사항**:
- Base64로 인코딩된 secret 사용 또는 UTF-8 명시
- Secret 길이 검증 추가 (최소 32바이트)

#### 3.2 JwtExceptionFilter 미구현
**위치**: \`JwtExceptionFilter.java\`

**문제점**:
- 빈 클래스로 JWT 예외가 제대로 처리되지 않음
- 인증 실패 시 명확한 에러 응답 없음
- 클라이언트가 실패 원인 파악 불가

**권장 사항**:
- OncePerRequestFilter를 상속하여 예외 처리 로직 구현
- 401 Unauthorized 응답과 함께 명확한 에러 메시지 반환

#### 3.3 만료된 Access Token 처리 로직 미구현
**위치**: \`JwtAuthenticationFilter.java:54-58\`

**문제점**:
- 만료된 토큰에 대한 처리가 없음
- 클라이언트가 토큰 갱신 시점을 알 수 없음
- 401 Unauthorized 응답이 없음

**권장 사항**:
- 만료된 토큰임을 명시적으로 응답
- 클라이언트에게 토큰 갱신 엔드포인트로 유도
- 별도의 \`/api/auth/refresh\` 엔드포인트 구현

### 🟡 보통 (High)

#### 3.4 Refresh Token 검증 로직 문제
**위치**: \`RefreshTokenService.java:24-35\`

**문제점**:
- Refresh Token이 만료되었을 수 있는데 \`validateToken()\` 호출
- 만료 vs 잘못된 토큰 구분 불가

**권장 사항**:
- Refresh Token은 Redis 존재 여부와 일치만 확인
- 만료 시간은 Redis TTL로 관리하므로 JWT 검증 불필요

#### 3.5 Token Rotation Race Condition
**위치**: \`RefreshTokenService.java:37-48\`

**문제점**:
- 기존 토큰 삭제 없이 덮어쓰기
- 동시 요청 시 여러 토큰이 유효할 수 있음

**권장 사항**:
- 기존 토큰 삭제 후 새 토큰 저장하여 원자성 보장

#### 3.6 Boolean null 처리 미흡
**위치**: \`JwtAuthenticationFilter.java:40-45\`

**권장 사항**:
- \`Boolean.TRUE.equals(isBlacklist)\` 패턴 사용

#### 3.7 블랙리스트 TTL 관리 부재
**문제점**:
- Access Token 만료 시간과 동일하게 TTL 설정 필요
- 그렇지 않으면 Redis 메모리 누수 가능

### 🟢 낮음 (Medium)

#### 3.8 CORS 설정 부재
**문제점**:
- 프론트엔드와 다른 도메인에서 API 호출 시 문제 발생

#### 3.9 Rate Limiting 부재
**문제점**:
- Brute Force 공격에 취약
- 토큰 탈취 시도 무제한 가능

#### 3.10 getEmail() 예외 처리 부재
**문제점**:
- 만료된 토큰에서 email 추출 시 ExpiredJwtException 발생
- Refresh 로직에서 만료된 Access Token의 email이 필요할 수 있음

#### 3.11 TokenDto 미구현
**문제점**:
- 빈 클래스로 토큰 응답 구조가 불명확

---

## 4. 아키텍처 개선 제안

### 4.1 토큰 Claims 확장
현재는 email만 포함하지만, 추가 정보 고려:
- \`roles\`: 권한 정보
- \`userId\`: 사용자 ID
- \`tokenType\`: access/refresh 구분
- \`jti\`: JWT ID (토큰 고유 식별자)

### 4.2 토큰 갱신 전용 엔드포인트
별도의 \`/api/auth/refresh\` 엔드포인트 구현 권장

### 4.3 Sliding Session 고려
- Refresh Token 사용 시 Refresh Token도 갱신 (현재 구현됨 ✅)
- Access Token 갱신 시점 명확화 필요

### 4.4 Multi-Device 지원
현재는 email당 하나의 Refresh Token만 저장:
- 여러 기기에서 동시 로그인 불가
- 개선: \`refresh:{email}:{deviceId}\` 형식으로 여러 토큰 저장

### 4.5 Refresh Token Family 패턴
더 강력한 보안을 위한 Token Family 체인 유지 고려

---

## 5. 설정 파일 검토

### 5.1 JWT 설정
- Access Token: 30분 - 적절 ✅
- Refresh Token: 14일 - 적절 ✅
- Secret은 환경변수로 관리 ✅
- Secret 최소 길이: 256비트 (32바이트) 이상 권장
- Base64 인코딩 사용 권장

### 5.2 Redis 설정
- 운영 환경에서는 Redis 인증 설정 필수
- Redis Sentinel 또는 Cluster 고려
- TTL 정책 명확화

---

## 6. 종합 평가

### ✅ 잘 구현된 부분
1. **Token Rotation**: Refresh Token 재사용 방지
2. **Reuse Detection**: 토큰 탈취 감지 메커니즘
3. **Blacklist**: 로그아웃된 토큰 관리
4. **STATELESS**: 확장 가능한 아키텍처
5. **Redis 활용**: 빠른 토큰 저장 및 조회

### ⚠️ 개선 필요 부분
1. **Secret Key 관리**: Base64 인코딩, 길이 검증
2. **JwtExceptionFilter**: 예외 처리 로직 구현
3. **만료 토큰 처리**: 명확한 응답 및 갱신 플로우
4. **Race Condition**: Token Rotation 원자성
5. **에러 응답**: 표준화된 에러 메시지

### 🔒 보안 강화 권장사항
1. **Rate Limiting**: 토큰 발급/갱신 제한
2. **CORS**: 적절한 도메인 제한
3. **블랙리스트 TTL**: 메모리 관리
4. **로깅**: 보안 이벤트 로깅
5. **모니터링**: 비정상 토큰 사용 패턴 감지

---

## 7. 결론

### 이론적 작동 여부
✅ **전체적으로 이론적으로는 잘 작동할 것으로 예상됩니다.**

핵심 JWT 인증 플로우는 올바르게 구현되었으며, Token Rotation과 Reuse Detection으로 보안이 강화되었습니다.

### 우선순위별 개선 작업

#### 🔴 즉시 수정 필요 (운영 배포 전 필수)
1. Secret Key Base64 인코딩 및 검증
2. JwtExceptionFilter 구현
3. 만료 토큰 처리 로직 구현
4. 블랙리스트 TTL 설정

#### 🟡 배포 후 조기 개선
1. CORS 설정
2. Rate Limiting
3. 에러 응답 표준화
4. TokenDto 구현

#### 🟢 장기 개선
1. Multi-Device 지원
2. Refresh Token Family 패턴
3. 토큰 Claims 확장
4. 보안 이벤트 로깅 및 모니터링

---

**리뷰 작성일**: 2025-12-04  
**리뷰어**: Claude Code  
**버전**: v1.0

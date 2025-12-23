# Cruisin - 음악/악기 이커머스 플랫폼

## JWT 인증 플로우

### 로그인 플로우
1. **AuthController** (`/api/auth/login`) - 로그인 요청 수신
2. **AuthService** - AuthenticationManager를 통한 인증 처리
3. **CustomUserDetailsService** - 이메일로 DB에서 사용자 정보 조회
4. **AuthenticationManager** - 비밀번호 검증 및 Authentication 객체 생성
5. **JwtTokenProvider** - Access Token 및 Refresh Token 생성
6. **RefreshTokenService** - Refresh Token을 Redis에 저장
7. **LoginResponseDto** 반환

### API 요청 시 인증 플로우
1. **JwtExceptionFilter** - JWT 예외 처리
2. **JwtAuthenticationFilter** - Authorization 헤더에서 Bearer 토큰 추출
3. **JwtAuthenticationFilter** - Redis 블랙리스트 검사 (로그아웃된 토큰 확인)
4. **JwtTokenProvider** - 토큰 유효성 검증
5. **JwtTokenProvider** - 토큰에서 이메일 추출 및 Authentication 객체 생성
6. **CustomUserDetailsService** - 이메일로 사용자 정보 로드
7. **JwtAuthenticationFilter** - SecurityContext에 인증 정보 설정
8. 이후 컨트롤러로 요청 전달

### 주요 클래스
- **JwtTokenProvider** - JWT 토큰 생성/검증/파싱
- **JwtAuthenticationFilter** - 요청마다 JWT 토큰 검증 및 인증 설정
- **JwtExceptionFilter** - JWT 관련 예외 처리 (만료, 서명 오류 등)
- **CustomUserDetailsService** - 사용자 정보 로드
- **RefreshTokenService** - Refresh Token 저장/검증/로테이션
- **RefreshTokenRepository** - Redis 기반 Refresh Token 저장소
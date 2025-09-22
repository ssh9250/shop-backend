# Issues Log

## Issue #001: 회원가입 시 서버 500 에러 발생

**발생일**: 2025-09-22

### 문제 상황
프론트엔드에서 회원가입 폼 제출 시 서버 500 에러 발생

### 원인 분석
Member 엔티티의 password 필드에 설정된 `@Size(max = 50)` 검증 어노테이션이 bcrypt 암호화된 비밀번호(60자 이상)와 충돌

### 해결 방법
```java
// 수정 전
@Column(nullable = false, length = 50)
@Size(min = 6, max = 50)
private String password;

// 수정 후
@Column(nullable = false, length = 200)
private String password;
```

### 관련 파일
- `src/main/java/com/study/shop/domain/member/entity/Member.java`

### 교훈
- 암호화된 데이터의 길이 고려: bcrypt 등 해시 함수 사용 시 결과 길이를 충분히 고려
- 검증 어노테이션 신중 사용: 엔티티 필드의 검증 어노테이션은 비즈니스 로직과 데이터 처리 과정을 모두 고려

---

## Issue #002: 전역 예외 핸들러로 인한 디버깅 어려움

**발생일**: 2025-09-22

### 문제 상황
모든 예외가 전역 예외 핸들러에서 일괄 처리되어 구체적인 에러 정보 확인 불가

### 원인 분석
```java
// 문제가 된 코드
@ExceptionHandler
public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
    return ResponseEntity.status(500)
            .body(ApiResponse.fail("서버 내부에 오류가 발생했습니다."));
}
```

### 해결 방법
```java
// 수정 후
@ExceptionHandler
public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
    e.printStackTrace(); // 스택트레이스 출력 추가
    return ResponseEntity.status(500)
            .body(ApiResponse.fail("서버 내부에 오류가 발생했습니다."));
}
```

### 관련 파일
- `src/main/java/com/study/shop/global/exception/GlobalExceptionHandler.java`

### 교훈
- 디버깅 친화적인 예외 처리: 개발 중에는 구체적인 에러 정보를 확인할 수 있도록 로깅 필요
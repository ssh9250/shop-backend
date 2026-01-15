package com.study.shop.global.exception;

import com.study.shop.global.response.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.websocket.AuthenticationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.List;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CustomException.class)
    public ResponseEntity<ApiResponse<Void>> handleCustomException(CustomException e) {
        ErrorCode code = e.getErrorCode();
        log.error("CustomException: {} - {}", code.name(), code.getMessage());
        return ResponseEntity.status(code.getStatus())
                .body(ApiResponse.fail(code.getMessage()));
    }

    // 임시로 Validation Error 잡기용
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationExceptions(MethodArgumentNotValidException e) {
        List<FieldError> fieldErrors = e.getBindingResult().getFieldErrors();

        return ResponseEntity.badRequest().body(ApiResponse.fail("Validation Error"));
    }

    @ExceptionHandler({
            BadCredentialsException.class,
            AuthenticationException.class
    })
    public ResponseEntity<ApiResponse<Void>> handleBadCredentialsException(Exception e) {
        // 보안상의 이유로 세분화하지 않는 것이 바람직
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.fail("로그인에 실패하였습니다."));
    }

    @ExceptionHandler
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        e.printStackTrace();
        return ResponseEntity.status(500)
                .body(ApiResponse.fail("서버 내부에 오류가 발생했습니다."));
    }
}

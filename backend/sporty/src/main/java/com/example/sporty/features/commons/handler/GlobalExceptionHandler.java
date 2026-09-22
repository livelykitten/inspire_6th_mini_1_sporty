package com.example.sporty.features.commons.handler;

import com.example.sporty.features.commons.exception.auth.LoginFailException;
import com.example.sporty.features.commons.exception.users.DuplicateEmailException;
import com.example.sporty.features.commons.exception.users.DuplicateNicknameException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(DuplicateEmailException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateEmail(
            DuplicateEmailException e
    ) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.builder()
                        .code("DUPLICATE_EMAIL")
                        .message(e.getMessage())
                        .build());
    }

    @ExceptionHandler(DuplicateNicknameException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateNickname(
            DuplicateNicknameException e
    ) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.builder()
                        .code("DUPLICATE_NICKNAME")
                        .message(e.getMessage())
                        .build());
    }

    @ExceptionHandler(LoginFailException.class)
    public ResponseEntity<ErrorResponse> handleLoginFail(
            LoginFailException e
    ) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ErrorResponse.builder()
                        .code("LOGIN_FAILED")
                        .message(e.getMessage())
                        .build());
    }
}

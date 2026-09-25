package com.example.sporty.features.commons.handler;

import com.example.sporty.features.commons.exception.auth.LoginFailException;
import com.example.sporty.features.commons.exception.exerciseMatching.MatchNotFoundException;
import com.example.sporty.features.commons.exception.exerciseMatching.MatchAlreadyJoinedException;
import com.example.sporty.features.commons.exception.exerciseMatching.MatchAlreadyStartedException;
import com.example.sporty.features.commons.exception.exerciseMatching.MatchFullException;
import com.example.sporty.features.commons.exception.exerciseMatching.MatchRecruitmentClosedException;
import com.example.sporty.features.commons.exception.users.DuplicateEmailException;
import com.example.sporty.features.commons.exception.users.DuplicateNicknameException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MatchAlreadyJoinedException.class)
    public ResponseEntity<ErrorResponse> handleMatchAlreadyJoined(MatchAlreadyJoinedException e) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body(ErrorResponse.builder()
                        .code("MATCH_ALREADY_JOINED")
                        .message(e.getMessage())
                        .build());
    }

    @ExceptionHandler(MatchRecruitmentClosedException.class)
    public ResponseEntity<ErrorResponse> handleMatchRecruitmentClosed(MatchRecruitmentClosedException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.builder()
                        .code("MATCH_RECRUITMENT_CLOSED")
                        .message(e.getMessage())
                        .build());
    }

    @ExceptionHandler(MatchAlreadyStartedException.class)
    public ResponseEntity<ErrorResponse> handleMatchAlreadyStarted(MatchAlreadyStartedException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.builder()
                        .code("MATCH_ALREADY_STARTED")
                        .message(e.getMessage())
                        .build());
    }

    @ExceptionHandler(MatchFullException.class)
    public ResponseEntity<ErrorResponse> handleMatchFull(MatchFullException e) {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.builder()
                        .code("MATCH_FULL")
                        .message(e.getMessage())
                        .build());
    }

    @ExceptionHandler(MatchNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleMatchNotFound(MatchNotFoundException e) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
                .body(ErrorResponse.builder()
                        .code("MATCH_NOT_FOUND")
                        .message(e.getMessage())
                        .build());
    }

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

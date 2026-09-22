package com.example.sporty.features.ai.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.sporty.features.ai.agent.MatchAiAgent;
import com.example.sporty.features.ai.domain.dto.AiRequestDto;
import com.example.sporty.features.commons.handler.ErrorResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/ai")
@RequiredArgsConstructor
public class AiController {

    private final MatchAiAgent matchAIAgent;

    // AI-02  body : { "prompt" : "이번 주말 강남에서 풋살 초보 매치 찾아줘" }
    @PostMapping("/matches/search")
    public ResponseEntity<?> searchMatches(@Valid @RequestBody AiRequestDto request) {
        System.out.println("debug >>>> ai controller searchMatches : " + request);
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(matchAIAgent.search(request.getPrompt()));
    }

    // 입력 검증 실패(@Valid) → 400 + 안내 문구
    // 이 컨트롤러에만 적용. 팀 공통 처리가 정해지면 GlobalExceptionHandler로 이동.
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleInvalidInput(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse("요청 내용을 확인해주세요.");

        System.out.println("debug >>>> ai controller invalid input : " + message);

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ErrorResponse.builder().message(message).build());
    }

    // TODO: AI-01 POST /matches, AI-03 GET /matches/recommendations
}
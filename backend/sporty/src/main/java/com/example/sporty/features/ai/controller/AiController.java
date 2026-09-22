package com.example.sporty.features.ai.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.sporty.features.ai.agent.MatchAiAgent;
import com.example.sporty.features.ai.domain.dto.AiRequestDto;

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

    // TODO: AI-01 POST /matches, AI-03 GET /matches/recommendations
}
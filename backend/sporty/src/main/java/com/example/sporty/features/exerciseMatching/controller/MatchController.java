package com.example.sporty.features.exerciseMatching.controller;

import org.springframework.web.bind.annotation.RestController;

import com.example.sporty.features.exerciseMatching.domain.dto.MatchCreateRequestDto;
import com.example.sporty.features.exerciseMatching.service.MatchService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.util.HashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;


@RestController 
@RequiredArgsConstructor 
public class MatchController {

    private final MatchService matchService;

    @PostMapping("/api/matches")
    public ResponseEntity<?> createMatch(
        @Valid @RequestBody MatchCreateRequestDto req,
        BindingResult bindingResult
    ) {
        System.out.println("debug >> MatchController.createMatch() called with: " + req);
        
        // check for validation errors
        if (bindingResult.hasErrors()) {
            Map<String, String> errMap = new HashMap<>();

            bindingResult.getAllErrors().forEach(e -> {
                FieldError field = (FieldError)e; 
                String msg = e.getDefaultMessage();
                errMap.put(field.getField(), msg);
            });
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errMap);
        }

        // proceed if there are no errors
        
        return ResponseEntity.status(HttpStatus.CREATED).body(matchService.createMatch(req));
    }
    
    
}

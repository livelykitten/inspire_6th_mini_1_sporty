package com.example.sporty.features.auth.controller;

import com.example.sporty.features.auth.domain.dto.LoginRequestDto;
import com.example.sporty.features.auth.domain.dto.LoginResponseDto;
import com.example.sporty.features.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("api/auth")
@RequiredArgsConstructor
@RestController
public class AuthController {

    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDto> login(
            @RequestBody LoginRequestDto request
    ) {
        LoginResponseDto response = authService.login(request);

        return ResponseEntity.ok(response);
    }
}

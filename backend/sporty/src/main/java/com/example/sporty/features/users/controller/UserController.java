package com.example.sporty.features.users.controller;

import com.example.sporty.features.users.domain.dto.UserSignUpRequestDto;
import com.example.sporty.features.users.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;


@RestController
@RequestMapping("api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping
    public ResponseEntity<Void> signUp(@RequestBody @Valid UserSignUpRequestDto request) {
        userService.signUp(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .build();
    }

    // [USR-04] 회원탈퇴
    @DeleteMapping("/me")
    public ResponseEntity<Void> withdrawal( @RequestBody Map<String, String> request) {

        userService.withdrawal(request.get("password"));

        return ResponseEntity.noContent().build();
    }



}

package com.example.sporty.features.auth.service;

import com.example.sporty.features.auth.domain.dto.LoginRequestDto;
import com.example.sporty.features.auth.domain.dto.LoginResponseDto;
import com.example.sporty.features.commons.exception.auth.LoginFailException;
import com.example.sporty.features.users.domain.entity.UserEntity;
import com.example.sporty.features.users.repository.UserRepository;
import com.example.sporty.features.commons.token.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;

    public LoginResponseDto login(LoginRequestDto request) {

        // 1. 이메일로 회원 조회
        UserEntity user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(LoginFailException::new);

        // 2. 비밀번호 확인
        if (!passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        )) {
            throw new LoginFailException();
        }

        // 3. JWT 생성
        String accessToken = jwtProvider.createAccessToken(
                user.getId()
        );

        // 4. 토큰 반환
        return new LoginResponseDto(accessToken);
    }
}
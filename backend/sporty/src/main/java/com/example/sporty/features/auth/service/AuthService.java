package com.example.sporty.features.auth.service;

import com.example.sporty.features.auth.domain.dto.LoginRequestDto;
import com.example.sporty.features.auth.domain.dto.LoginResponseDto;
import com.example.sporty.features.commons.exception.auth.LoginFailException;
import com.example.sporty.features.users.domain.entity.UserEntity;
import com.example.sporty.features.users.domain.entity.UserStatus;
import com.example.sporty.features.users.repository.UserRepository;
import com.example.sporty.features.commons.token.JwtProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;

    public LoginResponseDto login(LoginRequestDto request) {

        // 1. 이메일로 회원 조회
        UserEntity user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(LoginFailException::new);

        // 2. 정상 회원 여부 및 비밀번호 확인
        if (user.getStatus() != UserStatus.ACTIVE || !passwordEncoder.matches(
                request.getPassword(),
                user.getPassword()
        )) {
            throw new LoginFailException();
        }

        // 3. JWT 생성
        String accessToken = jwtProvider.createAccessToken(
                user.getId()
        );
        String refreshToken = jwtProvider.createRefreshToken(user.getId());

        // 4. Redis 저장에 실패하면 로그인 성공 응답을 반환하지 않습니다.
        refreshTokenService.save(user.getId(), refreshToken,
                jwtProvider.getRefreshTokenExpirationSeconds());

        // 5. 토큰 반환
        return LoginResponseDto.builder()
                .userId(user.getId())
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtProvider.getAccessTokenExpirationSeconds())
                .build();
    }
}

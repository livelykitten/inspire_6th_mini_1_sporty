package com.example.sporty.features.commons.token;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

@Component
public class JwtProvider {
    private static final Duration ACCESS_TOKEN_EXP = Duration.ofMinutes(30);
    private static final Duration REFRESH_TOKEN_EXP = Duration.ofDays(7);
    private final Key key;

    public JwtProvider(@Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(Long id) {
        return createToken(id, "ACCESS", ACCESS_TOKEN_EXP);
    }

    public String createRefreshToken(Long id) {
        return createToken(id, "REFRESH", REFRESH_TOKEN_EXP);
    }

    public long getAccessTokenExpirationSeconds() {
        return ACCESS_TOKEN_EXP.toSeconds();
    }

    public long getRefreshTokenExpirationSeconds() {
        return REFRESH_TOKEN_EXP.toSeconds();
    }

    private String createToken(Long id, String tokenType, Duration validity) {
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("토큰 발급에 유효한 회원 ID가 필요합니다.");
        }
        Instant now = Instant.now();
        return Jwts.builder()
                .setSubject(id.toString())
                .setId(UUID.randomUUID().toString())
                .claim("tokenType", tokenType)
                .setIssuedAt(Date.from(now))
                .setExpiration(Date.from(now.plus(validity)))
                .signWith(key)
                .compact();
    }

}

package com.example.sporty.features.commons.token;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;

@Component
public class JwtProvider {
    @Value("${jwt.secret}")
    private String secret;

    // ms 단위로 계산하는 것이 기본
    private final long ACCESS_TOKEN_EXP = 1000L * 60 * 30; // 30분
    private final long REFRESH_TOKEN_EXP = 1000L * 60 * 60 * 24 * 7; // 일주일

    private Key getSecretKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String createAccessToken(Long id) {
        System.out.println("debug >>> Provider createAT");
        return Jwts.builder()
                .setSubject(String.valueOf(id))// 발급 주체(로그인한 사용자)
                .setIssuedAt(new Date()) // 발급 시간
                .setExpiration(new Date(System.currentTimeMillis() + ACCESS_TOKEN_EXP)) // 유효 기간
                .signWith(getSecretKey())
                .compact();
    }

    public String createRefreshToken(Long id) {
        System.out.println("debug >>> Provider createRT");
        return Jwts.builder()
                .setSubject(String.valueOf(id)) // 발급 주체(로그인한 사용자)
                .setIssuedAt(new Date()) // 발급 시간
                .setExpiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXP)) // 유효 기간
                .signWith(getSecretKey())
                .compact();
    }

}

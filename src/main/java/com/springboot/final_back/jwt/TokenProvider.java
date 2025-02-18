package com.springboot.final_back.jwt;


import com.springboot.final_back.dto.TokenDto;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Component
@Slf4j
public class TokenProvider {
    private static final String AUTHORITIES_KEY = "auth";
    private final Key key;

    // 비밀키 기반 객체 초기화, @Value 는 스프링 어노테이션
    public TokenProvider(@Value("${jwt.secret}") String secretKey) {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes());
    }

    public TokenDto generateTokenDto(Authentication authentication) {
        // 인증 객체에서 권한 정보 추출
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        // 현재 시간과 토큰 만료 시간 계산
        long now = (new Date()).getTime();
        Date accessTokenExpiresIn = new Date(now + 30 * 60 * 1000); // 30분

        Date refreshTokenExpiresIn = new Date(now + 30 * 60 * 1000 * 48 * 6); // 6일

        // Access Token 생성
        String accessToken = Jwts.builder()
                .setSubject(authentication.getName()) // 사용자명 설정
                .claim(AUTHORITIES_KEY, authorities)  // 권한 정보 저장
                .setExpiration(accessTokenExpiresIn)  // 만료 시간 설정
                .signWith(key, SignatureAlgorithm.HS512) // 서명 방식 설정
                .compact();

        // RefreshToken 생성
        String refreshToken = Jwts.builder()
                .setSubject(authentication.getName()) // 사용자명 설정
                .claim(AUTHORITIES_KEY, authorities)  // 권한 정보 저장
                .setExpiration(refreshTokenExpiresIn)  // 만료 시간 설정
                .signWith(key, SignatureAlgorithm.HS512) // 서명 방식 설정
                .compact();

        // 결과를 DTO로 반환
        return TokenDto.builder()
                .grantType("Bearer")
                .accessToken(accessToken) // 액세스 토큰
                .accessTokenExpiresIn(accessTokenExpiresIn.getTime()) // 액세스 토큰 만료 시간
                .refreshToken(refreshToken) // 리프레시 토큰
                .refreshTokenExpiresIn(refreshTokenExpiresIn.getTime()) // 리프레시 토큰 만료 시간
                .build();
    }

    // 토큰에서 인증 객체 생성
    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);

        // 권한 정보 추출
        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        // 인증 객체 생성 후 반환
        User principal = new User(claims.getSubject(), "", authorities);
        return new UsernamePasswordAuthenticationToken(principal, token, authorities);
    }


    // 토큰 유효성 검증
    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            log.info("JWT 검증 실패: {}", e.getMessage());
        }
        return false;
    }

    // 토큰의 Claims(내용) 추출
    private Claims parseClaims(String token) {
        try {
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException e) {
            return e.getClaims();
        }
    }
}

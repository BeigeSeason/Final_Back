package com.springboot.final_back.jwt;


import com.springboot.final_back.dto.TokenDto;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Arrays;
import java.util.Collection;
import java.util.Date;
import java.util.stream.Collectors;

@Slf4j
@Component
public class TokenProvider {
    private static final String AUTHORITIES_KEY = "auth";
    private static final String BEARER_TYPE = "Bearer";
    private static final long ACCESS_TOKEN_EXPIRE_TIME = 1000 * 60 * 600; // 600분  = 1000 * 60 * 600
    private static final long REFRESH_TOKEN_EXPIRE_TIME = 7L * 24 * 60 * 60 * 1000; // 7일
    private final Key key;

    // springframework.beans.factory.annotation.Value
    // @Value : 설정 파일에서 JWT를 만들 때 사용할 암호화 키를 가져오기 위한 어노테이션
    public TokenProvider(@Value("${jwt.secret}") String secretKey) {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes()); // HS256 알고리즘으로 새로운 키를 생성
    }

    // 인증에 성공한 사용자의 인증 정보를 캡슐화한 객체를 매개 변수로 받아,
    public TokenDto generateTokenDto(Authentication authentication) {
        // 이를 문자열로 변환한 후, 이 정보를 이용해 토큰을 생성
        String authorities = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.joining(","));

        // 토큰 만료 시간 설정
        long now = (new Date()).getTime();
        Date accessTokenExpiresIn = new Date(now + ACCESS_TOKEN_EXPIRE_TIME);
        Date refreshTokenExpiresIn = new Date(now + REFRESH_TOKEN_EXPIRE_TIME);

        // Access Token 생성
        String accessToken = Jwts.builder()
                .setSubject(authentication.getName())
                .claim(AUTHORITIES_KEY, authorities)
                .setExpiration(accessTokenExpiresIn)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();

        // Refresh Token 생성
        String refreshToken = Jwts.builder()
                .setSubject(authentication.getName())
                .claim(AUTHORITIES_KEY, authorities)
                .setExpiration(refreshTokenExpiresIn)
                .signWith(key, SignatureAlgorithm.HS512)
                .compact();

        // 토큰 정보를 담은 TokenDto 객체 생성
        return TokenDto.builder()
                .grantType(BEARER_TYPE)
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .build();
    }

    // 토큰 복호화
    private Claims parseClaims(String token) {
        try {
            // 토큰의 헤더에 일반적으로 어떠한 알고리즘으로 암호화 되었는지가 기술되어있다. ( "alg" : "HS512" )
            return Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token).getBody();
        } catch (ExpiredJwtException e) {
            // ExpiredJwtException : 토큰이 만료됐다는 것을 의미
            return e.getClaims();
        }
    }

    // 로그인 함수에서 사용자가 입력한 정보를 토대로 토큰을 생성하고, 해당 토큰을 이용하여 인증을 시도해 성공 시 새로운 토큰을 생성했었다.
    // 고로 한 번의 복호화를 거치면 "사용자가 입력한 정보를 토대로 토큰을 생성" 의 토큰이 반환된다.
    public Authentication getAuthentication(String token) {
        Claims claims = parseClaims(token);

        if (claims.get(AUTHORITIES_KEY) == null) {
            throw new RuntimeException("권한 정보가 없는 토큰입니다.");
        }

        // 권한 정보 추출
        Collection<? extends GrantedAuthority> authorities =
                Arrays.stream(claims.get(AUTHORITIES_KEY).toString().split(","))
                        .map(SimpleGrantedAuthority::new)
                        .collect(Collectors.toList());

        UserDetails principal = new User(claims.getSubject(), "", authorities);

        return new UsernamePasswordAuthenticationToken(principal, "", authorities);
    }

    // 토큰의 유효성 검증
    public boolean validateToken(String token) {
        try {
            // 토큰을 파싱하여 유효하다면,
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (SecurityException | MalformedJwtException e) {
            log.warn("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.warn("만료된 JWT 토큰입니다."); // 이때, 401 에러를 반환
        } catch (UnsupportedJwtException e) {
            log.warn("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.warn("JWT 토큰이 잘못되었습니다.");
        }
        return false;
    }

    // access 토큰 재발급
    public String generateAccessToken(Authentication authentication) {
        return generateTokenDto(authentication).getAccessToken();
    }
}

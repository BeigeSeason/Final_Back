package com.springboot.final_back.security;

import com.springboot.final_back.jwt.JwtFilter;
import com.springboot.final_back.jwt.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.config.annotation.SecurityConfigurerAdapter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.DefaultSecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

// Spring Security 설정에 JwtFilter를 추가하여, HTTP 요청이 들어올 때 마다 JWT 인증을 수행하도록 설정
@RequiredArgsConstructor
public class JwtSecurityConfig extends SecurityConfigurerAdapter<DefaultSecurityFilterChain, HttpSecurity> {
    private final TokenProvider tokenProvider;

    @Override
    public void configure(HttpSecurity http) {
        JwtFilter customFilter = new JwtFilter(tokenProvider); // JwtFilter 객체 생성. http 요청을 인터셉트하여 JWT 인증을 수행
        http.addFilterBefore(customFilter, UsernamePasswordAuthenticationFilter.class);
    }
}

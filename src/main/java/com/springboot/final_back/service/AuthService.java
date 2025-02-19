package com.springboot.final_back.service;

import com.springboot.final_back.dto.loginDto;
import com.springboot.final_back.dto.TokenDto;
import com.springboot.final_back.jwt.TokenProvider;
import com.springboot.final_back.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class AuthService {
    private final AuthenticationManagerBuilder managerBuilder; // 인증을 담당하는 클래스
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;


    public TokenDto login(loginDto memberReqDto) {
        try{
            UsernamePasswordAuthenticationToken authenticationToken = memberReqDto.toAuthentication();

            // authenticate() 내부에서 loadUserByUsername()가 실행되어 가입한 회원인지 확인하는 로직 존재함
            Authentication authentication = managerBuilder.build().authenticate(authenticationToken);

            TokenDto tokenDto = tokenProvider.generateTokenDto(authentication);

            String RefreshToken = tokenDto.getRefreshToken();

            return null;

        }catch (Exception e){
            log.error(e.getMessage());
            return null;
        }
    }

}

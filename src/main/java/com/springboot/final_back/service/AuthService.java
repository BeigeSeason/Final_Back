package com.springboot.final_back.service;

import com.springboot.final_back.dto.LoginDto;
import com.springboot.final_back.dto.MemberResDto;
import com.springboot.final_back.dto.TokenDto;
import com.springboot.final_back.entity.Member;
import com.springboot.final_back.entity.RefreshToken;
import com.springboot.final_back.jwt.TokenProvider;
import com.springboot.final_back.repository.MemberRepository;
import com.springboot.final_back.repository.RefreshTokenRepository;
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
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;

    public MemberResDto signUp(MemberResDto memberResDto){
        return null;
    }

    public TokenDto login(LoginDto memberReqDto) {
        try{
            Member member = memberRepository.findByUserId(memberReqDto.getUserId())
                    .orElseThrow(()-> new RuntimeException("no"));

            UsernamePasswordAuthenticationToken authenticationToken = memberReqDto.toAuthentication();

            // authenticate() 내부에서 loadUserByUsername()가 실행되어 가입한 회원인지 확인하는 로직 존재함
            Authentication authentication = managerBuilder.build().authenticate(authenticationToken);

            TokenDto tokenDto = tokenProvider.generateTokenDto(authentication);

            String newRefreshToken = tokenDto.getRefreshToken();

            RefreshToken refreshToken = refreshTokenRepository.findByMember_UserId(memberReqDto.getUserId())
                    .orElse(null);

            if(refreshToken == null){
                RefreshToken newToken = RefreshToken.builder()
                        .refreshToken(newRefreshToken)
                        .expiresIn(tokenDto.getRefreshTokenExpiresIn())
                        .member(member)
                        .build();
                refreshTokenRepository.save(newToken);
            } else {
                refreshToken.update(newRefreshToken, tokenDto.getRefreshTokenExpiresIn());
            }

            return tokenDto;

        }catch (Exception e){
            log.error(e.getMessage());
            return null;
        }
    }

}

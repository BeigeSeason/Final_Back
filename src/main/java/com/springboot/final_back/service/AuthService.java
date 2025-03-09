package com.springboot.final_back.service;

import com.springboot.final_back.dto.*;
import com.springboot.final_back.dto.Auth.MemberReqDto;
import com.springboot.final_back.dto.Auth.SignupDto;
import com.springboot.final_back.dto.Auth.TokenDto;
import com.springboot.final_back.entity.mysql.Member;
import com.springboot.final_back.entity.mysql.RefreshToken;
import com.springboot.final_back.exception.NotMemberException;
import com.springboot.final_back.jwt.TokenProvider;
import com.springboot.final_back.repository.MemberRepository;
import com.springboot.final_back.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class AuthService {
    private final AuthenticationManager authenticationManager; // 인증을 담당하는 클래스
    private final MemberRepository memberRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;

    // 회원가입
    @Transactional
    public boolean signUp(SignupDto signupDto) {
        try{
            if(memberRepository.existsMemberByUserId(signupDto.getUserId())){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
            }else {
                memberRepository.save(signupDto.toEntity(passwordEncoder));
                return true;
            }
        }catch (ResponseStatusException e){
            log.error("회원 가입 실패 : {}",e.getMessage());
            throw e;
        }
    }

    // 로그인
    public TokenDto login(LoginDto memberReqDto) {
        try{
            Member member = memberRepository.findByUserId(memberReqDto.getUserId())
                    .orElseThrow(()-> new NotMemberException(HttpStatus.UNAUTHORIZED, "회원가입이 필요합니다."));

            UsernamePasswordAuthenticationToken authenticationToken = memberReqDto.toAuthentication();
            // authenticate() 내부에서 loadUserByUsername()가 실행되어 가입한 회원인지 확인하는 로직 존재함
            Authentication authentication = authenticationManager.authenticate(authenticationToken);
            TokenDto tokenDto = tokenProvider.generateTokenDto(authentication);
            String newRefreshToken = tokenDto.getRefreshToken();
            RefreshToken refreshToken = refreshTokenRepository.findByMember_UserId(memberReqDto.getUserId())
                    .orElse(null);
            if(refreshToken == null){
                RefreshToken newToken = RefreshToken.builder()
                        .refreshToken(newRefreshToken)
                        .member(member)
                        .build();
                refreshTokenRepository.save(newToken);
            } else {
                refreshToken.update(newRefreshToken);
            }

            return tokenDto;

        }catch (Exception e){
            log.error(e.getMessage());
            return null;
        }
    }

    // 액세스 토큰 재발급
    public String refreshAccessToken(String refreshToken) {
        // 리프레시 토큰 값에서 큰따옴표 제거 (프론트에서 전달 시 JSON 형태라 큰따옴표가 앞뒤로 붙어서 나옴)
        String trimmedToken = refreshToken.replace("\"", "").trim();
        Optional<RefreshToken> tokenOptional = refreshTokenRepository.findByRefreshToken(refreshToken);
        if (tokenOptional.isEmpty()) {
            throw new RuntimeException("리프레시 토큰이 존재하지 않습니다.");
        }

        // 리프레시 토큰 유효성 검증
        if (!tokenProvider.validateToken(trimmedToken)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "리프레시 토큰이 유효하지 않습니다.");
        }

        // 새 액세스 토큰 생성
        try {
            Authentication authentication = tokenProvider.getAuthentication(trimmedToken);
            return tokenProvider.generateAccessToken(authentication);
        } catch (RuntimeException e) {
            log.error("토큰 생성 실패: {}", e.getMessage(), e);
            throw new RuntimeException("토큰 생성에 실패했습니다.", e);
        }
    }

    // 내 정보 수정
    @Transactional
    public boolean updateMember(MemberReqDto memberReqDto) {
        try{
            Member member = memberRepository.findByUserId(memberReqDto.getUserId()).orElseThrow(() ->  new RuntimeException("Member not found"));
            member.setName(memberReqDto.getName());
            member.setNickname(memberReqDto.getNickname());
            memberRepository.save(member);
            return true;
        } catch (Exception e) {
            log.error("회원정보 수정 오류 : {}", e.getMessage());
            return false;
        }
    }
}

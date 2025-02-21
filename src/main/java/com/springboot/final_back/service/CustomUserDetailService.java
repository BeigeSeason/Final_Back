package com.springboot.final_back.service;

import com.springboot.final_back.constant.MemberRole;
import com.springboot.final_back.entity.mysql.Member;
import com.springboot.final_back.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

// 스프링 시큐리티에서 제공하는 UserDetailsService 인터페이스를 구현한 클래스로, 사용자의 인증 정보를 불러오는 것이 주 역할
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailService implements UserDetailsService {
    private final MemberRepository memberRepository;

    // 생성된 UserDetails는 Service의 login()에서 Authentication 을 생성할 때 authenticate() 메소드 내부에서 loadUserByUserName이 호출
    @Override
    public UserDetails loadUserByUsername(String userid) throws UsernameNotFoundException {
        Member member = memberRepository.findByUserId(userid).orElseThrow(()
                -> new RuntimeException("사용자 찾을 수 없음"));

        return createUserDetails(member);
    }

    // DB에서 가져온 권한 정보를 UserDetails로 변환
    private UserDetails createUserDetails(Member member) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER")); // 기본적으로 ROLE_USER 권한 부여

        // 멤버의 Role이 어드민인 경우 ROLE_ADMIN 권한 추가
        if (member.getRole() == MemberRole.ADMIN) { // member.getRole() 이 어드민 Role Enum 값과 일치하는지 확인
            authorities.add(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }

        return new User(member.getUserId(), member.getPassword(), authorities);
    }

}

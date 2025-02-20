package com.springboot.final_back.dto;

import com.springboot.final_back.entity.Member;
import lombok.*;
import org.springframework.security.crypto.password.PasswordEncoder;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SignupDto {
    private String userId;
    private String password;
    private String email;
    private String name;
    private String nickname;
    private String imgPath;
    private String sso;
    private String ssoId;

    public Member toEntity(PasswordEncoder passwordEncoder) {
        return Member.ssoBuilder()
                .userId(userId)
                .password(passwordEncoder.encode(password))
                .email(email)
                .name(name)
                .nickname(nickname)
                .imgPath(imgPath)
                .sso(sso)
                .ssoId(ssoId)
                .build();
    }

}

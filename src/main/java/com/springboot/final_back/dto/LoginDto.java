package com.springboot.final_back.dto;

import lombok.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class LoginDto {
    private String userId;
    private String password;


    public UsernamePasswordAuthenticationToken toAuthentication() {
        return new UsernamePasswordAuthenticationToken(userId, password);
    }
}

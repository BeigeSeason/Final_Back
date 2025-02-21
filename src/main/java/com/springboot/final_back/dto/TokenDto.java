package com.springboot.final_back.dto;

import lombok.*;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TokenDto {
    private String grantType; // 인증 방식
    private String accessToken; // 액세스 토큰
    private String refreshToken; // 리프레시 토큰
}

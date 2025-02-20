package com.springboot.final_back.dto;

import lombok.*;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberReqDto {
    private String userId;
    private String password;
    private String email;
    private String name;
    private String nickname;
    private String imgPath;
}

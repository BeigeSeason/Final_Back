package com.springboot.final_back.dto;

import lombok.*;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberResDto {
    private String memberId;
    private String email;
    private String name;
    private String nickname;
    private String imgPath;
    private boolean banned;

}

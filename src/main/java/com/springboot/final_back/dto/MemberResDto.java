package com.springboot.final_back.dto;

import com.springboot.final_back.entity.mysql.Member;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MemberResDto {
    private Long id;
    private String userId;
    private String email;
    private String name;
    private String nickname;
    private String imgPath;
    private String sso;
    private String ssoId;
    private LocalDateTime regDate;
    private boolean banned;

    public static MemberResDto of(Member member) {
        return MemberResDto.builder()
                .id(member.getId())
                .userId(member.getUserId())
                .email(member.getEmail())
                .name(member.getName())
                .nickname(member.getNickname())
                .imgPath(member.getImgPath())
                .sso(member.getSso())
                .ssoId(member.getSsoId())
                .regDate(member.getRegDate())
                .banned(member.isBanned())
                .build();
    }
}

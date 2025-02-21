package com.springboot.final_back.dto;

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
    private LocalDateTime regDate;
    private boolean banned;

}

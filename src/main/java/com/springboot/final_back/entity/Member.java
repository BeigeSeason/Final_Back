package com.springboot.final_back.entity;

import com.springboot.final_back.constant.UserRole;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "member")
@ToString
@Getter
@Setter
@RequiredArgsConstructor
@NoArgsConstructor
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String memberId;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String nickname;

    private LocalDateTime regDate;

    private String sso;

    private String ssoId;

    private UserRole role;

    private boolean banned;

    @PrePersist
    protected void onCreate() {
        regDate = LocalDateTime.now();
        banned = false;
        role = UserRole.USER;
    }

    @Builder
    private Member(String memberId, String password, String email, String name, String nickname){
        this.memberId = memberId;
        this.password = password;
        this.email = email;
        this.name = name;
        this.nickname = nickname;
    }

    @Builder
    private Member(String memberId, String password, String email, String name, String nickname, String sso, String ssoId){
        this.memberId = memberId;
        this.password = password;
        this.email = email;
        this.name = name;
        this.nickname = nickname;
        this.sso = sso;
        this.ssoId = ssoId;
    }
}

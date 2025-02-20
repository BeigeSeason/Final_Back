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
public class Member {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String userId;

    @Column(nullable = false)
    private String password;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String name;

    @Column(unique = true, nullable = false)
    private String nickname;

    private String imgPath;

    private String sso;

    private String ssoId;

    private LocalDateTime regDate;

    private UserRole role;

    private boolean banned;

    @PrePersist
    protected void onCreate() {
        regDate = LocalDateTime.now();
        banned = false;
        role = UserRole.USER;
    }

    @Builder
    private Member(String userId, String password, String email, String name, String nickname, String imgPath) {
        this.userId = userId;
        this.password = password;
        this.email = email;
        this.name = name;
        this.nickname = nickname;
        this.imgPath = imgPath;
    }

    // 생성자 오버로딩이 가능하지만 명시적으로 구분
    @Builder(builderMethodName = "ssoBuilder")
    private Member(String userId, String password, String email, String name, String nickname, String imgPath, String sso, String ssoId){
        this.userId = userId;
        this.password = password;
        this.email = email;
        this.name = name;
        this.nickname = nickname;
        this.imgPath = imgPath;
        this.sso = sso;
        this.ssoId = ssoId;
    }

}

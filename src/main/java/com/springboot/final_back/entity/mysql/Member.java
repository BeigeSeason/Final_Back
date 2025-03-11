package com.springboot.final_back.entity.mysql;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.springboot.final_back.constant.MemberRole;
import com.springboot.final_back.dto.Auth.MemberResDto;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    @JsonIgnore
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

    @Enumerated(EnumType.STRING)
    private MemberRole role;

    private boolean banned;

    @PrePersist
    protected void onCreate() {
        regDate = LocalDateTime.now();
        banned = false;
        role = MemberRole.USER;
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

    public MemberResDto convertEntityToDto() {
        MemberResDto memberResDto = new MemberResDto();
        memberResDto.setId(this.getId());
        memberResDto.setUserId(this.getUserId());
        memberResDto.setEmail(this.getEmail());
        memberResDto.setName(this.getName());
        memberResDto.setNickname(this.getNickname());
        memberResDto.setImgPath(this.getImgPath());
        memberResDto.setRegDate(this.getRegDate());
        memberResDto.setBanned(this.isBanned());
        return memberResDto;
    }

    @OneToMany(mappedBy = "reporter", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Report> reporter = new ArrayList<>();

    @OneToMany(mappedBy = "reported", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Report> reported = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "member", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Bookmark> bookmarks = new ArrayList<>();

    @OneToMany(mappedBy = "member_id", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<Ban> bans = new ArrayList<>();
}

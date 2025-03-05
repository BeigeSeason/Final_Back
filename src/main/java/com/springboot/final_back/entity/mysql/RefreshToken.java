package com.springboot.final_back.entity.mysql;

import lombok.*;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "refreshtoken")
@ToString
@Getter
@Setter
@RequiredArgsConstructor
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Lob
    private String refreshToken;

    private LocalDateTime createdAt;

    private LocalDateTime updatedAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member")
    private Member member;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @Builder
    public RefreshToken(Long id, String refreshToken, Member member) {
        this.id = id;
        this.refreshToken = refreshToken;
        this.member = member;
    }

    public void update(String refreshToken) {
        this.refreshToken = refreshToken;
        this.updatedAt = LocalDateTime.now();
    }
}

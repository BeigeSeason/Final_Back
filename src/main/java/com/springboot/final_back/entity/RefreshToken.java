package com.springboot.final_back.entity;

import com.springboot.final_back.constant.Type;
import lombok.*;
import org.springframework.security.core.parameters.P;

import javax.persistence.*;
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
    @Column(unique = true, nullable = false)
    private String refreshToken;

    private Long expiresIn;

    private LocalDateTime createdAt;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member")
    private Member member;

    @PrePersist
    public void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @Builder
    public RefreshToken(Long id, String refreshToken, Long expiresIn, Member member) {
        this.id = id;
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
        this.member = member;
    }

    public void update(String refreshToken, Long expiresIn) {
        this.refreshToken = refreshToken;
        this.expiresIn = expiresIn;
    }
}

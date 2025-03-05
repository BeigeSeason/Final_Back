package com.springboot.final_back.entity.mysql;

import lombok.*;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "review")
@ToString
@Getter
@Setter
@RequiredArgsConstructor
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member")
    private Member member;

    @Column(nullable = false)
    private int rating;

    @Column(nullable = false)
    private String tourSpotId;

    private String content;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @Builder
    private Review(Member member, int rating, String tourSpotId, String content) {
        this.member = member;
        this.rating = rating;
        this.tourSpotId = tourSpotId;
        this.content = content;
    }
}

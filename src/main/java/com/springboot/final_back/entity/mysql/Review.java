package com.springboot.final_back.entity.mysql;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.*;

import javax.persistence.*;
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
    @JsonIgnore
    private Member member;

    @Column(nullable = false)
    private float rating;

    @Column(nullable = false)
    private String tourSpotId;

    private String content;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }

    @Builder
    private Review(Member member, float rating, String tourSpotId, String content) {
        this.member = member;
        this.rating = rating;
        this.tourSpotId = tourSpotId;
        this.content = content;
    }
}

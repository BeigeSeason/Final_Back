package com.springboot.final_back.entity.mysql;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Table(name="Ban")
@Entity
@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Ban {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ban_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    private LocalDateTime startDate;
    private LocalDateTime endDate;

    private String reason;

    private boolean isEnd;

    @Builder
    public Ban(Member member, LocalDateTime startDate, LocalDateTime endDate, String reason) {
        this.member = member;
        this.startDate = startDate;
        this.endDate = endDate;
        this.reason = reason;
        this.isEnd = false;
    }
}

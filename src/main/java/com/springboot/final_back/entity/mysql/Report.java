package com.springboot.final_back.entity.mysql;


import com.springboot.final_back.constant.State;
import com.springboot.final_back.constant.Type;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "report")
@ToString
@Getter
@Setter
@RequiredArgsConstructor
public class Report {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 신고 타입(멤버, 다이어리, 댓글(리뷰)
    @Enumerated(EnumType.STRING)
    @Column(name = "report_type")
    private Type reportType; // MEMBER, DIARY, REVIEW, TOURSPOT

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "reporter")
    private Member reporter;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "reported")
    private Member reported;

    private String reportEntity; // 다이어리id, 리뷰id

    @Lob
    private String reason;

    private LocalDateTime createdAt;

    private LocalDateTime checkedAt;

    @Enumerated(EnumType.STRING)
    private State state;

    @PrePersist
    public void onCreate() {
        state = State.WAIT;
        createdAt = LocalDateTime.now();
    }

    @Builder
    public Report(Long id, Type reportType, Member reporter, Member reported, String reportEntity, String reason) {
        this.id = id;
        this.reportType = reportType;
        this.reporter = reporter;
        this.reported = reported;
        this.reportEntity = reportEntity;
        this.reason = reason;
        this.state = State.WAIT;
    }
}

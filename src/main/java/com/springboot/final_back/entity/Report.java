package com.springboot.final_back.entity;


import com.springboot.final_back.constant.State;
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

    @Enumerated(EnumType.STRING)
    private ReportType reportType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reporter")
    private Member reporter;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reported")
    private Member reported;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diary_id", nullable = true)
    private Long diaryId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "review_id", nullable = true)
    private Long reviewId;

    @Lob
    private String content;

    private LocalDateTime createdAt;

    private LocalDateTime checkedAt;

    private State state;

    public enum ReportType {
        USER, DIARY, REVIEW
    }

    @PrePersist
    public void onCreate() {
        state = State.WAIT;
        createdAt = LocalDateTime.now();
    }

    @Builder
    public Report(Long id, Member reporter, Member reported, String content) {
        this.id = id;
        this.reporter = reporter;
        this.reported = reported;
        this.content = content;
        this.state = State.WAIT;
    }
}

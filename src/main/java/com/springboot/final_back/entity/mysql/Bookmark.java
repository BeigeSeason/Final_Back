package com.springboot.final_back.entity.mysql;

import com.springboot.final_back.constant.Type;
import lombok.*;

import javax.persistence.*;

@Entity
@Table(name = "bookmark")
@ToString
@Getter
@Setter
@RequiredArgsConstructor
@Builder
@AllArgsConstructor
public class Bookmark {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member")
    private Member member;

    private Type type; // MEMBER, DIARY, REVIEW, TOURSPOT

    private String bookmarkedId; // Diary 아이디, TourSpot 아이디
}

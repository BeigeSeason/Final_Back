package com.springboot.final_back.service;

import com.springboot.final_back.dto.DiaryReqDto;
import com.springboot.final_back.entity.elasticsearch.Diary;
import com.springboot.final_back.entity.mysql.Member;
import com.springboot.final_back.repository.DiaryRepository;
import com.springboot.final_back.repository.MemberRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@AllArgsConstructor
public class DiaryService {
    private DiaryRepository diaryRepository;
    private MemberRepository memberRepository;

    // 다이어리 생성
    public boolean createDiary(DiaryReqDto dto) {
        try{
            Member member = memberRepository.findByUserId(dto.getUserId()).orElseThrow(()-> new RuntimeException("Member not found"));
            Long memberId = member.getId();

            Diary diary = Diary.builder()
                    .title(dto.getTitle())
                    .region(dto.getRegion())
                    .startDate(dto.getStartDate())
                    .endDate(dto.getEndDate())
                    .tags(dto.getTags())
                    .totalCost(dto.getTotalCost())
                    .content(dto.getContent())
                    .memberId(memberId)
                    .build();

            diaryRepository.save(diary);

            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

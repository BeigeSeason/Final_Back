package com.springboot.final_back.service;

import com.springboot.final_back.dto.DiaryReqDto;
import com.springboot.final_back.entity.elasticsearch.Diary;
import com.springboot.final_back.entity.mysql.Member;
import com.springboot.final_back.repository.DiaryRepository;
import com.springboot.final_back.repository.MemberRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

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
                    .diaryId(dto.getDiaryId())
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

    // 다이어리 삭제
    public boolean deleteDiary(String diaryId) {
        try {
            Diary diary = diaryRepository.findById(diaryId)
                    .orElseThrow(() -> new RuntimeException("해당 일기를 찾을 수 없습니다."));
            diaryRepository.delete(diary);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

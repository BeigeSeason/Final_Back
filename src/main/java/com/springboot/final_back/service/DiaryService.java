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

    // Diary id 마지막번호 가져오기
    public Long getLastDiaryId() {
        Map<String, Object> result = diaryRepository.findLastId();
        if (result == null || !result.containsKey("aggregations")) {
            return 0L; // diary가 아예 없을 경우 기본값 반환
        }
        Map<String, Object> aggregations = (Map<String, Object>) result.get("aggregations");
        Map<String, Object> maxId = (Map<String, Object>) aggregations.get("max_id");
//        if (maxId == null || !maxId.containsKey("value") || maxId.get("value") == null) {
        if (maxId.get("value") == null) {
            return 0L; // 다이어리가 없는 경우 0L 반환(0은 기본적으로 int이지만, OL로 하면 long으로 인식)
        }
        return ((Number) maxId.get("value")).longValue();
    }

    // 다이어리 생성
    public String createDiary(DiaryReqDto dto) {
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

            Diary savedDiary = diaryRepository.save(diary);

            return savedDiary.getId();
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

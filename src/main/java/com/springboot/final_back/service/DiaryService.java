package com.springboot.final_back.service;

import com.springboot.final_back.dto.DiaryReqDto;
import com.springboot.final_back.dto.DiaryResDto;
import com.springboot.final_back.entity.elasticsearch.Diary;
import com.springboot.final_back.entity.mysql.Member;
import com.springboot.final_back.repository.DiaryRepository;
import com.springboot.final_back.repository.MemberRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

@Slf4j
@Service
@AllArgsConstructor
public class DiaryService {
    private DiaryRepository diaryRepository;
    private MemberRepository memberRepository;

    // 다이어리 생성
    @Transactional
    public boolean createDiary(DiaryReqDto dto) {
        try{
            Member member = memberRepository.findByUserId(dto.getUserId()).orElseThrow(()-> new RuntimeException("Member not found"));
            Long memberId = member.getId();

            Diary diary = Diary.builder()
                    .diaryId(dto.getDiaryId())
                    .title(dto.getTitle())
                    .region(dto.getRegion())
                    .areaCode(dto.getAreaCode())
                    .sigunguCode(dto.getSigunguCode())
                    .startDate(dto.getStartDate())
                    .endDate(dto.getEndDate())
                    .tags(dto.getTags())
                    .totalCost(dto.getTotalCost())
                    .content(dto.getContent())
                    .memberId(memberId)
                    .isPublic(dto.isPublic())
                    .build();

            diaryRepository.save(diary);

            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 다이어리 수정
    @Transactional
    public boolean editDiary (DiaryReqDto diaryReqDto) {
        try{
            Diary diary = diaryRepository.findByDiaryId(diaryReqDto.getDiaryId())
                    .orElseThrow(() -> new RuntimeException("해당 일기를 찾을 수 없습니다."));
            diary.setTitle(diaryReqDto.getTitle());
            diary.setRegion(diaryReqDto.getRegion());
            diary.setAreaCode(diaryReqDto.getAreaCode());
            diary.setSigunguCode(diaryReqDto.getSigunguCode());
            diary.setStartDate(diaryReqDto.getStartDate());
            diary.setEndDate(diaryReqDto.getEndDate());
            diary.setTags(diaryReqDto.getTags());
            diary.setTotalCost(diaryReqDto.getTotalCost());
            diary.setContent(diaryReqDto.getContent());
            diary.setPublic(diaryReqDto.isPublic());
            diary.setCreatedTime(LocalDateTime.now());

            diaryRepository.save(diary);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 다이어리 삭제
    @Transactional
    public boolean deleteDiary(String diaryId) {
        try {
            Diary diary = diaryRepository.findByDiaryId(diaryId)
                    .orElseThrow(() -> new RuntimeException("해당 일기를 찾을 수 없습니다."));
            diaryRepository.delete(diary);
            return true;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // 다이어리 상세조회
    public DiaryResDto getDiaryDetail(String diaryId) {
        Diary diary = diaryRepository.findByDiaryId(diaryId).orElseThrow(() ->  new RuntimeException("Diary not found"));
        Member member = memberRepository.findById(diary.getMemberId()).orElseThrow(() ->  new RuntimeException("Member not found"));
        String nickname = member.getNickname();
        String ownerId = member.getUserId();
        String imgPath = member.getImgPath();
        return DiaryResDto.fromEntity(diary, nickname, ownerId, imgPath);
    }

    // 다이어리 공개/비공개 전환
    @Transactional
    public boolean changeIsPublic(String diaryId, boolean isPublic) {
        try {
            Diary diary = diaryRepository.findByDiaryId(diaryId).orElseThrow(() -> new RuntimeException("Diary not found"));
            diary.setPublic(isPublic);
            diaryRepository.save(diary);
            return true;
        } catch (Exception e) {
            log.error("다이어리 공개/비공개 변경 중 에러: {}", e.getMessage());
            return false;
        }
    }
}

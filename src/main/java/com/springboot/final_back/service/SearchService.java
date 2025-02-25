package com.springboot.final_back.service;


import com.springboot.final_back.dto.search.DiarySearchListDto;
import com.springboot.final_back.dto.search.TourSpotListDto;
import com.springboot.final_back.entity.elasticsearch.Diary;
import com.springboot.final_back.entity.elasticsearch.TourSpots;
import com.springboot.final_back.entity.mysql.Member;
import com.springboot.final_back.repository.DiaryRepository;
import com.springboot.final_back.repository.MemberRepository;
import com.springboot.final_back.repository.TourSpotsRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class SearchService {
    private final TourSpotsRepository tourSpotsRepository;
    private final DiaryRepository diaryRepository;
    private final MemberRepository memberRepository;

    public Page<DiarySearchListDto> searchByTitle(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Diary> diaryPage =diaryRepository.findByTitle(keyword, pageable);
        if(diaryPage.isEmpty()){
            return Page.empty();
        }

        List<Long> memberIdList = diaryPage.getContent().stream()
                .map(Diary::getMemberId).toList();

        List<Member> memberList = memberRepository.findByIdIn(memberIdList);

        Map<Long, Member> memberMap = memberList.stream()
                .collect(Collectors.toMap(Member::getId, member -> member)); // memberId를 키로 하는 Map 생성

        List<DiarySearchListDto> dtoList = diaryPage.getContent().stream()
                .map(diary -> {
                    Member author = memberMap.get(diary.getMemberId()); // memberId로 작성자 정보 조회
                    return DiarySearchListDto.builder()
                            .title(diary.getTitle()) // 다이어리 정보
                            .contentSummary(diary.getContent().length() > 150 ? diary.getContent().substring(0, 150) + "..." : diary.getContent()) // 150자가 넘어가면 150자만 보여주기
                            .thumbnail(null)
                            .writer(author.getNickname())
                            .writerImg(author.getImgPath() != null ? author.getImgPath() : null)
                            .createdAt(diary.getCreatedTime())
                            .build();
                })
                .toList();

        return new PageImpl<>(dtoList, pageable, diaryPage.getTotalElements());
    }

    public Page<TourSpotListDto> searchTourSpots(int page, int size, String keyword) {
        Pageable pageable = PageRequest.of(page, size);
        Page<TourSpots> pages;
        if(keyword == null){
            pages = tourSpotsRepository.findAll(pageable);
        }else {
            pages = tourSpotsRepository.findByTitleOrAddr1(keyword, keyword, pageable);
        }

        return pages.map(TourSpots::convertToListDto);
    }

}

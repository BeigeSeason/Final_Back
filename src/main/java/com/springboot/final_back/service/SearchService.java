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
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;

import static org.elasticsearch.index.query.QueryBuilders.*;

@Slf4j
@Service
@AllArgsConstructor
public class SearchService {
    private final TourSpotsRepository tourSpotsRepository;
    private final ElasticsearchOperations elasticsearchOperations;
    private final DiaryRepository diaryRepository;
    private final MemberRepository memberRepository;

    public Page<DiarySearchListDto> searchByTitle(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Diary> diaryPage = diaryRepository.findByTitle(keyword, pageable);
        if (diaryPage.isEmpty()) {
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

    public Page<TourSpotListDto> searchTourSpots(int page, int size, int sort, String keyword,
                                                 String areaCode, String sigunguCode, String contentTypeId) {
        Sort sortOrder;
        switch (sort) {
            case 0:
                sortOrder = Sort.by(Sort.Direction.ASC, "title");
            case 1:
                sortOrder = Sort.by(Sort.Direction.DESC, "title");
            default:
                sortOrder = Sort.by(Sort.Direction.ASC, "title");
        }

//        = sort != null ? Sort.by(Sort.Direction.fromString(sort.split(",")[1]), sort.split(",")[0]) :
//                Sort.by(Sort.Direction.ASC, "title.keyword");
        Pageable pageable = PageRequest.of(page, size, sortOrder);

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        // 검색어 처리
        if (keyword != null && !keyword.isEmpty()) {
            boolQuery.must(QueryBuilders.multiMatchQuery(keyword, "title", "addr1"));
        } else {
            boolQuery.must(QueryBuilders.matchAllQuery());
        }

        // 필터링
        if (areaCode != null) boolQuery.filter(QueryBuilders.termQuery("area_code", areaCode));
        if (sigunguCode != null) boolQuery.filter(QueryBuilders.termQuery("sigungu_code", sigunguCode));
        if (contentTypeId != null) boolQuery.filter(QueryBuilders.termQuery("content_type_id", contentTypeId));

        Query query = new NativeSearchQueryBuilder()
                .withQuery(boolQuery)
                .withPageable(pageable)
                .build();

        SearchHits<TourSpots> searchHits = elasticsearchOperations.search(query, TourSpots.class);
        return new PageImpl<>(
                searchHits.getSearchHits().stream().map(SearchHit::getContent).map(TourSpots::convertToListDto).collect(Collectors.toList()),
                pageable,
                searchHits.getTotalHits()
        );
    }

}

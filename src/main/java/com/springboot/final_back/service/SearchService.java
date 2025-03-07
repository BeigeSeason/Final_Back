package com.springboot.final_back.service;

import com.springboot.final_back.dto.search.DiarySearchListDto;
import com.springboot.final_back.dto.search.TourSpotListDto;
import com.springboot.final_back.entity.elasticsearch.Diary;
import com.springboot.final_back.entity.elasticsearch.TourSpots;
import com.springboot.final_back.entity.mysql.Member;
import com.springboot.final_back.repository.MemberRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class SearchService {
    private final ElasticsearchOperations elasticsearchOperations;
    private final MemberRepository memberRepository;

    // 제목으로 다이어리 검색
    public Page<DiarySearchListDto> searchByTitle(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        if (keyword == null || keyword.isEmpty()) {
            boolQuery.must(QueryBuilders.matchAllQuery());
        } else {
            boolQuery.must(QueryBuilders.matchQuery("title", keyword));
        }

        Query query = new NativeSearchQueryBuilder()
                .withQuery(boolQuery)
                .withPageable(pageable)
                .build();

        SearchHits<Diary> searchHits = elasticsearchOperations.search(query, Diary.class);
        if (searchHits.isEmpty()) {
            log.debug("No diaries found for keyword: {}", keyword);
            return Page.empty(pageable);
        }

        List<Diary> diaries = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());
        Map<Long, Member> memberMap = getMemberMap(diaries);

        List<DiarySearchListDto> dtoList = mapToDiaryDtoList(diaries, memberMap);
        return new PageImpl<>(dtoList, pageable, searchHits.getTotalHits());
    }

    // 관광지 검색
    public Page<TourSpotListDto> searchTourSpots(int page, int size, String sort, String keyword,
                                                 String areaCode, String sigunguCode, String contentTypeId) {
        Sort sortOrder = sort != null && !sort.isEmpty() ?
                Sort.by(Sort.Direction.fromString(sort.split(",")[1]), sort.split(",")[0]) :
                Sort.by(Sort.Direction.ASC, "title_sort");
        Pageable pageable = PageRequest.of(page, size, sortOrder);

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        boolean hasFilters = keyword != null || areaCode != null || sigunguCode != null || contentTypeId != null;
        if (!hasFilters) {
            log.debug("No filters provided, performing full search");
            boolQuery.must(QueryBuilders.matchAllQuery());
        } else {
            if (keyword != null && !keyword.isEmpty()) {
                boolQuery.must(QueryBuilders.multiMatchQuery(keyword, "title", "addr1"));
            }
            if (areaCode != null) boolQuery.filter(QueryBuilders.termQuery("area_code", areaCode));
            if (sigunguCode != null) boolQuery.filter(QueryBuilders.termQuery("sigungu_code", sigunguCode));
            if (contentTypeId != null) boolQuery.filter(QueryBuilders.termQuery("content_type_id", contentTypeId));
        }

        Query query = new NativeSearchQueryBuilder()
                .withQuery(boolQuery)
                .withPageable(pageable)
                .build();

        SearchHits<TourSpots> searchHits = elasticsearchOperations.search(query, TourSpots.class);
        List<TourSpotListDto> dtoList = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .map(TourSpots::convertToListDto)
                .collect(Collectors.toList());

        return new PageImpl<>(dtoList, pageable, searchHits.getTotalHits());
    }

    // 나의 다이어리 목록 조회 (비공개 포함)
    public Page<DiarySearchListDto> getMyDiaryList(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Member author = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Member not found"));
        log.info("{}, {}, {}", userId, page, size);

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery()
                .filter(QueryBuilders.termQuery("member_id", author.getId()));

        log.info("Query: {}", boolQuery.toString());

        Query query = new NativeSearchQueryBuilder()
                .withQuery(boolQuery)
                .withPageable(pageable)
                .build();
        log.info("Query: {}", query.toString());

        SearchHits<Diary> searchHits = elasticsearchOperations.search(query, Diary.class);
        if (searchHits.isEmpty()) {
            log.warn("No diaries found for userId: {}", userId);
            return Page.empty(pageable);
        }



        List<Diary> diaries = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());
        Map<Long, Member> memberMap = Map.of(author.getId(), author); // 단일 멤버만 필요

        List<DiarySearchListDto> dtoList = mapToDiaryDtoList(diaries, memberMap);
        return new PageImpl<>(dtoList, pageable, searchHits.getTotalHits());
    }

    // 특정 유저 다이어리 목록 조회 (비공개 미포함)
    public Page<DiarySearchListDto> getOtherUserDiaryList(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Member author = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery()
                .filter(QueryBuilders.termQuery("member_id", author.getId()))
                .filter(QueryBuilders.termQuery("is_public", true));

        Query query = new NativeSearchQueryBuilder()
                .withQuery(boolQuery)
                .withPageable(pageable)
                .build();

        SearchHits<Diary> searchHits = elasticsearchOperations.search(query, Diary.class);
        if (searchHits.isEmpty()) {
            log.debug("No diaries found for userId: {}", userId);
            return Page.empty(pageable);
        }

        List<Diary> diaries = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());
        Map<Long, Member> memberMap = Map.of(author.getId(), author); // 단일 멤버만 필요

        List<DiarySearchListDto> dtoList = mapToDiaryDtoList(diaries, memberMap);
        return new PageImpl<>(dtoList, pageable, searchHits.getTotalHits());
    }

    // 공통 메서드: Diary 리스트를 DTO로 변환
    private List<DiarySearchListDto> mapToDiaryDtoList(List<Diary> diaries, Map<Long, Member> memberMap) {
        return diaries.stream()
                .map(diary -> {
                    Member author = memberMap.get(diary.getMemberId());
                    return DiarySearchListDto.builder()
                            .diaryId(diary.getDiaryId())
                            .title(diary.getTitle())
                            .contentSummary(diary.getContent().length() > 150 ?
                                    diary.getContent().substring(0, 150) + "..." :
                                    diary.getContent())
                            .thumbnail(extractFirstImageSrc(diary.getContent()))
                            .writer(author.getNickname())
                            .writerImg(author.getImgPath() != null ? author.getImgPath() : null)
                            .createdAt(diary.getCreatedTime())
                            .build();
                })
                .toList();
    }

    // 공통 메서드: Member 맵 생성
    private Map<Long, Member> getMemberMap(List<Diary> diaries) {
        List<Long> memberIdList = diaries.stream()
                .map(Diary::getMemberId)
                .distinct()
                .toList();
        return memberRepository.findByIdIn(memberIdList)
                .stream()
                .collect(Collectors.toMap(Member::getId, member -> member));
    }

    // 정규표현식을 통한 HTML 파싱
    private String extractFirstImageSrc(String content) {
        if (content == null || !content.contains("<img")) {
            return null;
        }
        try {
            Pattern pattern = Pattern.compile("<img\\s+[^>]*src=['\"](.*?)['\"]");
            Matcher matcher = pattern.matcher(content);
            if (matcher.find()) {
                return matcher.group(1);
            }
            return null;
        } catch (Exception e) {
            log.warn("Failed to extract image src from content: {}", content, e);
            return null;
        }
    }
}

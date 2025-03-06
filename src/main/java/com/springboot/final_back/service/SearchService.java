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

        return searchAndMap(query, Diary.class, pageable, "keyword: " + keyword,
                diaries -> {
                    Map<Long, Member> memberMap = getMemberMap(diaries);
                    return mapToDiaryDtoList(diaries, memberMap);
                });
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

        return searchAndMap(query, TourSpots.class, pageable, "tour spots",
                tourSpots -> tourSpots.stream().map(TourSpots::convertToListDto).toList());
    }

    // 나의 다이어리 목록 조회 (비공개 포함)
    public Page<DiarySearchListDto> getMyDiaryList(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Member author = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery()
                .filter(QueryBuilders.termQuery("memberId", author.getId()));

        Query query = new NativeSearchQueryBuilder()
                .withQuery(boolQuery)
                .withPageable(pageable)
                .build();

        return searchAndMap(query, Diary.class, pageable, "userId: " + userId,
                diaries -> {
                    Map<Long, Member> memberMap = Map.of(author.getId(), author);
                    return mapToDiaryDtoList(diaries, memberMap);
                });
    }

    // 특정 유저 다이어리 목록 조회 (비공개 미포함)
    public Page<DiarySearchListDto> getOtherUserDiaryList(String userId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Member author = memberRepository.findByUserId(userId)
                .orElseThrow(() -> new RuntimeException("Member not found"));

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery()
                .filter(QueryBuilders.termQuery("memberId", author.getId()))
                .filter(QueryBuilders.termQuery("isPublic", true));

        Query query = new NativeSearchQueryBuilder()
                .withQuery(boolQuery)
                .withPageable(pageable)
                .build();

        return searchAndMap(query, Diary.class, pageable, "userId: " + userId,
                diaries -> {
                    Map<Long, Member> memberMap = Map.of(author.getId(), author);
                    return mapToDiaryDtoList(diaries, memberMap);
                });
    }

    // 공통 검색 및 매핑 메서드
    private <T, R> Page<R> searchAndMap(Query query, Class<T> entityClass, Pageable pageable, String logContext,
                                        Function<List<T>, List<R>> mapper) {
        SearchHits<T> searchHits = elasticsearchOperations.search(query, entityClass);
        if (searchHits.isEmpty()) {
            log.debug("No results found for {}", logContext);
            return Page.empty(pageable);
        }

        List<T> entities = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());
        List<R> dtoList = mapper.apply(entities);

        return new PageImpl<>(dtoList, pageable, searchHits.getTotalHits());
    }

    // Diary 리스트를 DTO로 변환
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

    // Member 맵 생성
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
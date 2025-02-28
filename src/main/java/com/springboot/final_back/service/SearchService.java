package com.springboot.final_back.service;


import com.springboot.final_back.constant.TourConstants;
import com.springboot.final_back.dto.search.DiarySearchListDto;
import com.springboot.final_back.dto.search.TourSpotListDto;
import com.springboot.final_back.dto.search.TourSpotStats;
import com.springboot.final_back.entity.elasticsearch.Diary;
import com.springboot.final_back.entity.elasticsearch.TourSpots;
import com.springboot.final_back.entity.mysql.Member;
import com.springboot.final_back.repository.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;


import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.transaction.annotation.Transactional;


@Slf4j
@Service
@AllArgsConstructor
public class SearchService {
    private final TourSpotsRepository tourSpotsRepository;
    private final ElasticsearchOperations elasticsearchOperations;
    private final DiaryRepository diaryRepository;
    private final MemberRepository memberRepository;
    private final RedisTemplate<String, TourSpotStats> redisTemplate;
    private final ReviewRepository reviewRepository;
    private final BookmarkRepository bookmarkRepository;

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

    // 관광지 검색, 필터링, 정렬
    @Transactional(readOnly = true)
    public Page<TourSpotListDto> searchTourSpots(int page, int size, String sort, String keyword,
                                                 String areaCode, String sigunguCode, String contentTypeId) {
        Sort defaultSort = Sort.by(Sort.Direction.ASC, "title.keyword");

        Pageable pageable = PageRequest.of(page, size, defaultSort);

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
            .withQuery(boolQuery) // boolQuery 적용
                .withPageable(pageable)
                .build();

        // 엘라스틱 서치에서 관광지 검색
        SearchHits<TourSpots> searchHits = elasticsearchOperations.search(query, TourSpots.class);

        // 검색된 관광지의 contentId 목록 추출
        List<String> contentIds = searchHits.getSearchHits().stream()
                .map(hit -> hit.getContent().getContentId())
                .collect(Collectors.toList());

        // 관광지 통계 데이터 (리뷰 수, 평균 평점, 북마크 수) 가져오기.
        Map<String, TourSpotStats> statsMap = getTourSpotStats(contentIds);

        // 검색 결과 Dto로 변환 후 정렬
        List<TourSpotListDto> content = searchHits.getSearchHits().stream()
                .map(hit -> {
                    TourSpots spot = hit.getContent();
                    TourSpotStats stats = statsMap.get(spot.getContentId());
                    TourSpotListDto dto = spot.convertToListDto();
                    dto.setReviewCount(stats != null ? stats.getReviewCount() : 0);
                    dto.setAvgRating(stats != null ? stats.getAvgRating() : 0);
                    dto.setBookmarkCount(stats != null ? stats.getBookmarkCount() : 0);
                    return dto;
                })
                .sorted(getComparator(sort))
                .toList();


        return new PageImpl<>(content, pageable, searchHits.getTotalHits());
    }

    // 관광지에 대한 통계(북마크, 리뷰) 데이터를 Redis 캐시에서 가져오고, 없다면 Mysql에서 조회 후 캐싱
    private Map<String, TourSpotStats> getTourSpotStats(List<String> contentIds) {
        Map<String, TourSpotStats> statsMap = new HashMap<>();
        List<String> uncachedIds = new ArrayList<>();

        for (String id : contentIds) {
            String cacheKey = "tour_spot_stats:" + id;
            TourSpotStats stats = redisTemplate.opsForValue().get(cacheKey);
            if (stats != null) {
                statsMap.put(id, stats);
            } else {
                uncachedIds.add(id);
            }
        }

        if (!uncachedIds.isEmpty()) {
            List<TourSpotStats> dbStats = fetchStatsFromMySQL(uncachedIds);
            dbStats.forEach(stats -> {
                String cacheKey = "tour_spot_stats:" + stats.getContentId();
                redisTemplate.opsForValue().set(cacheKey, stats, 1, TimeUnit.HOURS);
                statsMap.put(stats.getContentId(), stats);
            });
        }

        return statsMap;
    }

    // 여러 관광지의 리뷰/북마크 통계를 MySQL에서 조회해 TourSpotStats 리스트로 반환.
    private List<TourSpotStats> fetchStatsFromMySQL(List<String> contentIds) {
        List<Object[]> reviewStats = reviewRepository.findStatsByTourSpotIds(contentIds);
        List<Object[]> bookmarkStats = bookmarkRepository.findBookmarkCountsByTourSpotIds(contentIds);
        Map<String, TourSpotStats> statsMap = new HashMap<>();
        reviewStats.forEach(stat -> {
            String id = (String) stat[0];
            int reviewCount = ((Number) stat[1]).intValue();
            double avgRating = stat[2] != null ? ((Number) stat[2]).doubleValue() : 0.0;
            statsMap.put(id, new TourSpotStats(id, reviewCount, avgRating, 0));
        });
        bookmarkStats.forEach(stat -> {
            String id = (String) stat[0];
            int bookmarkCount = ((Number) stat[1]).intValue();
            TourSpotStats stats = statsMap.getOrDefault(id, new TourSpotStats(id, 0, 0.0, 0));
            stats.setBookmarkCount(bookmarkCount);
            statsMap.put(id, stats);
        });
        return new ArrayList<>(statsMap.values());
    }

    // 정렬 기준에 따라 비교기 생성
    // 기본값은 title 오름차순
    // , 기준으로 분리하여 필드명/방향(오름차순, 내림차순) 설정
    private Comparator<TourSpotListDto> getComparator(String sort) {
        if (sort == null || sort.isEmpty()) return Comparator.comparing(TourSpotListDto::getTitle);

        String[] parts = sort.split(",");
        String field = parts[0].toLowerCase();
        boolean isAsc = "asc".equalsIgnoreCase(parts[1]);

        Comparator<TourSpotListDto> comparator = switch (field) {
            case "reviewcount" -> Comparator.comparingInt(TourSpotListDto::getReviewCount);
            case "avgrating" -> Comparator.comparingDouble(TourSpotListDto::getAvgRating);
            case "bookmarkcount" -> Comparator.comparingInt(TourSpotListDto::getBookmarkCount);
            default -> Comparator.comparing(TourSpotListDto::getTitle);
        };
        return isAsc ? comparator : comparator.reversed();
    }


    // 비동기로 단일 관광지의 통계를 MySql 에서 가져와 Redis에 캐싱
    @Async
    public void cacheTourSpotStats(String tourSpotId) {
        TourSpotStats stats = fetchStatsFromMySQL(tourSpotId);
        String cacheKey = TourConstants.TOUR_SPOT_STATS_PREFIX + tourSpotId;
        redisTemplate.opsForValue().set(cacheKey, stats, 1, TimeUnit.HOURS);
    }

    // 단일 관광지의 리뷰/북마크 통계를 MySQL에서 조회해 TourSpotStats로 반환.
    private TourSpotStats fetchStatsFromMySQL(String tourSpotId) {
        Integer reviewCount = reviewRepository.countByReviewedId(tourSpotId);
        Double avgRating = reviewRepository.avgRatingByReviewedId(tourSpotId);
        Integer bookmarkCount = bookmarkRepository.countByBookmarkedId(tourSpotId);
        return new TourSpotStats(tourSpotId, reviewCount != null ? reviewCount : 0,
                avgRating != null ? avgRating : 0.0, bookmarkCount != null ? bookmarkCount : 0);
    }
}

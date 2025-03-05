package com.springboot.final_back.service;

import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.json.JsonData;
import co.elastic.clients.json.JsonpMapper;
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
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.StringQuery;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class SearchService {
    private final ElasticsearchOperations elasticsearchOperations;
    private final DiaryRepository diaryRepository;
    private final MemberRepository memberRepository;
    private ElasticsearchClient elasticsearchClient;
    private final JsonpMapper jsonpMapper;

    public Page<DiarySearchListDto> searchByTitle(String keyword, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        Page<Diary> diaryPage;

        if (keyword == null || keyword.isEmpty()) {
            // keyword 없으면 전체 조회
            StringQuery searchQuery = new StringQuery(
                    "{\"match_all\": {}}") // MatchAll 쿼리
                    .setPageable(pageable);
            SearchHits<Diary> searchHits = elasticsearchOperations.search(searchQuery, Diary.class);
            diaryPage = new PageImpl<>(
                    searchHits.getSearchHits().stream().map(SearchHit::getContent).collect(Collectors.toList()),
                    pageable,
                    searchHits.getTotalHits()
            );
        } else {
            // keyword 있으면 제목 검색
            diaryPage = diaryRepository.findByTitle(keyword, pageable);
        }

        if (diaryPage.isEmpty()) {
            log.debug("No diaries found for keyword: {}", keyword);
            return Page.empty(pageable);
        }

        List<Long> memberIdList = diaryPage.getContent().stream()
                .map(Diary::getMemberId)
                .toList();

        List<Member> memberList = memberRepository.findByIdIn(memberIdList);

        Map<Long, Member> memberMap = memberList.stream()
                .collect(Collectors.toMap(Member::getId, member -> member));

        List<DiarySearchListDto> dtoList = diaryPage.getContent().stream()
                .map(diary -> {
                    Member author = memberMap.get(diary.getMemberId());
                    return DiarySearchListDto.builder()
                            .diaryId(diary.getDiaryId())
                            .title(diary.getTitle())
                            .contentSummary(diary.getContent().length() > 150 ? diary.getContent().substring(0, 150) + "..." : diary.getContent())
                            .thumbnail(extractFirstImageSrc(diary.getContent()))
                            .writer(author.getNickname())
                            .writerImg(author.getImgPath() != null ? author.getImgPath() : null)
                            .createdAt(diary.getCreatedTime())
                            .build();
                })
                .toList();

        return new PageImpl<>(dtoList, pageable, diaryPage.getTotalElements());
    }


    public Page<TourSpotListDto> searchTourSpots(int page, int size, String sort, String keyword,
                                                 String areaCode, String sigunguCode, String contentTypeId) throws Exception {
        // 정렬 설정
        String sortField = sort != null && !sort.isEmpty() ? sort.split(",")[0] : "title.korean_sorted";
        SortOrder sortOrder = sort != null && !sort.isEmpty() ?
                SortOrder.valueOf(sort.split(",")[1].toUpperCase()) : SortOrder.Asc;

        // 페이지네이션 설정
        Pageable pageable = PageRequest.of(page, size,
                org.springframework.data.domain.Sort.by(
                        sortOrder == SortOrder.Asc ?
                                org.springframework.data.domain.Sort.Direction.ASC :
                                org.springframework.data.domain.Sort.Direction.DESC,
                        sortField));

        // 쿼리 빌드
        SearchResponse<TourSpots> response;
        try {
            response = elasticsearchClient.search(s -> s
                            .index("tour_spots")
                            .query(q -> q
                                    .bool(b -> {
                                        boolean hasFilters = keyword != null || areaCode != null || sigunguCode != null || contentTypeId != null;
                                        if (!hasFilters) {
                                            b.must(m -> m.matchAll(ma -> ma));
                                        } else {
                                            if (keyword != null && !keyword.isEmpty()) {
                                                b.must(m -> m.multiMatch(mm -> mm
                                                        .fields("title", "addr1")
                                                        .query(keyword)));
                                            }
                                            if (areaCode != null) {
                                                b.filter(f -> f.term(t -> t.field("area_code").value(areaCode)));
                                            }
                                            if (sigunguCode != null) {
                                                b.filter(f -> f.term(t -> t.field("sigungu_code").value(sigunguCode)));
                                            }
                                            if (contentTypeId != null) {
                                                b.filter(f -> f.term(t -> t.field("content_type_id").value(contentTypeId)));
                                            }
                                        }
                                        return b;
                                    }))
                            .from(page * size)
                            .size(size)
                            .sort(so -> so.field(f -> f.field(sortField).order(sortOrder))),
                    TourSpots.class);
        } catch (Exception e) {
            log.error("Failed to execute Elasticsearch query: {}", e.getMessage(), e);
            throw e;
        }

        // TourSpots -> TourSpotListDto 변환
        List<TourSpotListDto> dtoList = response.hits().hits().stream()
                .map(hit -> {
                    TourSpots spot = hit.source();
                    if (spot == null) {
                        log.warn("Null source in search hit: {}", hit.id());
                        return null;
                    }
                    return TourSpotListDto.builder()
                            .spotId(spot.getContentId())
                            .title(spot.getTitle())
                            .addr(spot.getAddr1())  // addr1 사용, 필요 시 addr2 결합
                            .thumbnail(spot.getFirstImage())
                            .reviewCount((int) spot.getReviewCount())
                            .avgRating(spot.getRating())
                            .bookmarkCount((int) spot.getBookmarkCount())
                            .build();
                })
                .filter(dto -> dto != null)  // null 필터링
                .collect(Collectors.toList());

        // Page 객체 생성
        long totalHits = response.hits().total().value();
        Page<TourSpotListDto> resultPage = new PageImpl<>(dtoList, pageable, totalHits);

        log.info("Search completed. Total hits: {}", totalHits);
        return resultPage;
    }

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
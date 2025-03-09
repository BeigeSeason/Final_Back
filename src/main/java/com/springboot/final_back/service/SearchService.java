package com.springboot.final_back.service;

import com.springboot.final_back.dto.diary.DiarySearchListDto;
import com.springboot.final_back.dto.tourspot.TourSpotListDto;
import com.springboot.final_back.entity.elasticsearch.Diary;
import com.springboot.final_back.entity.elasticsearch.TourSpots;
import com.springboot.final_back.entity.mysql.Member;
import com.springboot.final_back.repository.MemberRepository;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
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
import java.util.stream.Collectors;

@Slf4j
@Service
@AllArgsConstructor
public class SearchService {
    private final ElasticsearchOperations elasticsearchOperations;
    private final MemberRepository memberRepository;

    // 제목으로 다이어리 검색
    public Page<DiarySearchListDto> searchByTitle(int page, int size, String keyword, String sort,
                                                  int minPrice, int maxPrice, String areaCode, String sigunguCode) {
        Sort defaultSort = Sort.by(Sort.Direction.DESC, "_score");

        Sort sortOrder;
        if (sort != null && !sort.isEmpty()) {
            String[] split = sort.split(",");
            String field = split[0];
            Sort.Direction direction = Sort.Direction.fromString(split[1]);

            sortOrder = Sort.by(direction, field);
        } else {
            sortOrder = defaultSort;
        }

        // 가나다순, 북마크순, 최근작성순, 최근여행순, 여행경비 범위 지정
        Pageable pageable = PageRequest.of(page, size, sortOrder);
        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();


        boolean hasFilters = keyword != null || areaCode != null || sigunguCode != null || minPrice != 0 || maxPrice != 0;

        if (!hasFilters) {
            boolQuery.must(QueryBuilders.matchAllQuery());
        } else {
            if (keyword != null && !keyword.isEmpty()) {
                boolQuery.must(QueryBuilders.multiMatchQuery(keyword, "title", "content", "region"));
            }
            if (areaCode != null) boolQuery.filter(QueryBuilders.termQuery("area_code", areaCode));
            if (sigunguCode != null) boolQuery.filter(QueryBuilders.termQuery("sigungu_code", sigunguCode));
            if (minPrice != 0 && maxPrice != 0) {
                boolQuery.filter(QueryBuilders.rangeQuery("total_cost").gte(minPrice).lte(maxPrice));
            } else if (minPrice != 0) {
                // 이상(초과는 gt())
                boolQuery.filter(QueryBuilders.rangeQuery("total_cost").gte(minPrice));
            } else if (maxPrice != 0) {
                // 이하(미만은 lt())
                boolQuery.filter(QueryBuilders.rangeQuery("total_cost").lte(maxPrice));
            }
        }

        boolQuery.filter(QueryBuilders.termQuery("is_public", true));

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
                                                 String areaCode, String sigunguCode, String classifiedTypeId) {
        // 기본 정렬: chat_type ASC, title.keyword ASC
        Sort defaultSort = Sort.by(Sort.Direction.DESC, "_score");

        // 프론트엔드에서 sort가 제공된 경우
        Sort sortOrder;
        if (sort != null && !sort.isEmpty()) {
            String[] sortParts = sort.split(",");
            String field = sortParts[0];
            Sort.Direction direction = Sort.Direction.fromString(sortParts[1]);

            // chat_type과 title.keyword를 조합한 정렬
            if ("title".equals(field)) {  // 프론트에서 "title"로 정렬 요청 시
                sortOrder = Sort.by(direction, "char_type")
                        .and(Sort.by(direction, "title.sort"));
            } else {
                // 다른 필드에 대한 정렬 요청은 그대로 처리
                sortOrder = Sort.by(direction, field);
            }
        } else {
            sortOrder = defaultSort;
        }

        Pageable pageable = PageRequest.of(page, size, sortOrder);

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();
        boolean hasFilters = keyword != null || areaCode != null || sigunguCode != null || classifiedTypeId != null;
        if (!hasFilters) {
            log.debug("No filters provided, performing full search");
            boolQuery.must(QueryBuilders.matchAllQuery());
        } else {
            if (keyword != null && !keyword.isEmpty()) {
                boolQuery.must(QueryBuilders.multiMatchQuery(keyword, "title", "addr1"));
            }
            if (areaCode != null) boolQuery.filter(QueryBuilders.termQuery("area_code", areaCode));
            if (sigunguCode != null) boolQuery.filter(QueryBuilders.termQuery("sigungu_code", sigunguCode));
            if (classifiedTypeId != null) boolQuery.filter(QueryBuilders.termQuery("classified_type_id", classifiedTypeId));
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


        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery()
                .filter(QueryBuilders.termQuery("member_id", author.getId()));


        Query query = new NativeSearchQueryBuilder()
                .withQuery(boolQuery)
                .withPageable(pageable)
                .build();


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
                    String plainContent = stripHtmlTags(diary.getContent());
                    return DiarySearchListDto.builder()
                            .diaryId(diary.getDiaryId())
                            .title(diary.getTitle())
                            .contentSummary(plainContent.length() > 150 ?
                                    plainContent.substring(0, 150) + "..." :
                                    plainContent)
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

    // Jsoup 통한 HTML 태그 벗기기
    private String stripHtmlTags(String content) {
        if (content == null) return "";
        return Jsoup.parse(content).text().trim();
    }

    // Jsoup 통한 HTML 파싱
    private String extractFirstImageSrc(String content) {
        if (content == null || !content.contains("<img")) {
            return null;
        }
        try {
            Document doc = Jsoup.parse(content);
            Element img = doc.selectFirst("img"); // 첫 번째 <img> 태그 선택
            if (img != null) {
                return img.attr("src"); // src 속성 반환
            }
            return null;
        } catch (Exception e) {
            log.warn("Failed to extract image src from content: {}", content, e);
            return null;
        }
    }

    /*
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
    }*/
}

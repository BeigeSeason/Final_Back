package com.springboot.final_back.service;

import com.springboot.final_back.constant.Type;
import com.springboot.final_back.dto.diary.DiarySearchListDto;
import com.springboot.final_back.dto.tourspot.TourSpotListDto;
import com.springboot.final_back.entity.elasticsearch.Diary;
import com.springboot.final_back.entity.elasticsearch.TourSpots;
import com.springboot.final_back.entity.mysql.Bookmark;
import com.springboot.final_back.entity.mysql.Member;
import com.springboot.final_back.exception.ResourceNotFoundException;
import com.springboot.final_back.repository.BookmarkRepository;
import com.springboot.final_back.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
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

import java.util.*;
import java.util.stream.Collectors;

import static org.elasticsearch.index.query.QueryBuilders.*;

/**
 * 검색 관련 서비스
 * 다이어리 및 관광지 검색, 북마크 조회 등의 기능을 제공합니다.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    // 상수 정의
    private static final int CONTENT_SUMMARY_MAX_LENGTH = 150;
    private static final int RECOMMEND_COUNT = 10;
    private static final float PHRASE_MATCH_BOOST = 10.0f;
    private static final float PARTIAL_MATCH_BOOST = 1.0f;

    // 의존성 주입
    private final ElasticsearchOperations elasticsearchOperations;
    private final MemberRepository memberRepository;
    private final BookmarkRepository bookmarkRepository;

    /**
     * 다이어리 검색
     *
     * @param page 페이지 번호 (0부터 시작)
     * @param size 페이지 크기
     * @param keyword 검색 키워드 ('#'으로 시작하면 태그 검색)
     * @param sort 정렬 방식 (필드명,방향)
     * @param minPrice 최소 가격
     * @param maxPrice 최대 가격
     * @param areaCode 지역 코드
     * @param sigunguCode 시군구 코드
     * @return 다이어리 검색 결과
     */
    public Page<DiarySearchListDto> diarySearch(int page, int size, String keyword, String sort,
                                                int minPrice, int maxPrice, String areaCode, String sigunguCode) {

        log.debug("다이어리 검색: keyword={}, page={}, size={}, area={}, sigungu={}, price={}~{}",
                keyword, page, size, areaCode, sigunguCode, minPrice, maxPrice);

        // 페이징 및 정렬 설정
        Pageable pageable = createPageable(page, size, sort, Sort.by(Sort.Direction.DESC, "_score"));

        // 쿼리 빌더 생성 및 검색 조건 설정
        BoolQueryBuilder boolQuery = createDiarySearchQuery(keyword, minPrice, maxPrice, areaCode, sigunguCode);
        boolQuery.filter(termQuery("is_public", true));

        Query query = buildQuery(boolQuery, pageable);

        // 검색 실행
        SearchHits<Diary> searchHits = elasticsearchOperations.search(query, Diary.class);
        if (searchHits.isEmpty()) {
            log.debug("검색 결과 없음: keyword={}", keyword);
            return Page.empty(pageable);
        }

        // 결과 처리
        List<Diary> diaries = extractContentsFromHits(searchHits);
        Map<Long, Member> memberMap = getMemberMap(diaries);
        List<DiarySearchListDto> dtoList = mapToDiaryDtoList(diaries, memberMap);

        return new PageImpl<>(dtoList, pageable, searchHits.getTotalHits());
    }

    /**
     * 다이어리 검색 쿼리 생성
     */
    private BoolQueryBuilder createDiarySearchQuery(String keyword, int minPrice, int maxPrice,
                                                    String areaCode, String sigunguCode) {
        BoolQueryBuilder boolQuery = boolQuery();

        boolean hasFilters = keyword != null || areaCode != null || sigunguCode != null || minPrice != 0 || maxPrice != 0;

        if (!hasFilters) {
            boolQuery.must(QueryBuilders.matchAllQuery());
            return boolQuery;
        }

        // 키워드 검색 처리
        if (keyword != null && !keyword.isEmpty()) {
            addKeywordSearchCondition(boolQuery, keyword);
        }

        // 필터 조건 추가
        addLocationFilter(boolQuery, areaCode, sigunguCode);
        addPriceRangeFilter(boolQuery, minPrice, maxPrice);

        return boolQuery;
    }

    /**
     * 키워드 검색 조건 추가
     */
    private void addKeywordSearchCondition(BoolQueryBuilder boolQuery, String keyword) {
        if (keyword.startsWith("#")) {
            addTagSearchCondition(boolQuery, keyword);
        } else {
            // 일반 검색: title, content, region에서 검색
            boolQuery.must(QueryBuilders.multiMatchQuery(keyword, "title.ngram", "content", "region"));
        }
    }

    /**
     * 태그 검색 조건 추가
     */
    private void addTagSearchCondition(BoolQueryBuilder boolQuery, String tagKeyword) {
        String[] tagArray = tagKeyword.split("\\s+");
        List<String> tags = Arrays.stream(tagArray)
                .filter(tag -> !tag.isEmpty())
                .toList();

        if (!tags.isEmpty()) {
            BoolQueryBuilder tagQuery = boolQuery();
            for (String tag : tags) {
                tagQuery.should(QueryBuilders.termQuery("tags", tag));
            }
            tagQuery.minimumShouldMatch(1); // 최소 1개 태그 매칭
            boolQuery.must(tagQuery);
        }
    }

    /**
     * 지역 필터 추가
     */
    private void addLocationFilter(BoolQueryBuilder boolQuery, String areaCode, String sigunguCode) {
        if (areaCode != null) {
            boolQuery.filter(termQuery("area_code", areaCode));
        }
        if (sigunguCode != null) {
            boolQuery.filter(termQuery("sigungu_code", sigunguCode));
        }
    }

    /**
     * 가격 범위 필터 추가
     */
    private void addPriceRangeFilter(BoolQueryBuilder boolQuery, int minPrice, int maxPrice) {
        if (minPrice != 0 && maxPrice != 0) {
            boolQuery.filter(QueryBuilders.rangeQuery("total_cost").gte(minPrice).lte(maxPrice));
        } else if (minPrice != 0) {
            boolQuery.filter(QueryBuilders.rangeQuery("total_cost").gte(minPrice));
        } else if (maxPrice != 0) {
            boolQuery.filter(QueryBuilders.rangeQuery("total_cost").lte(maxPrice));
        }
    }

    /**
     * 관광지 검색
     *
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @param sort 정렬 방식
     * @param keyword 검색 키워드
     * @param areaCode 지역 코드
     * @param sigunguCode 시군구 코드
     * @param classifiedTypeId 분류 타입 ID
     * @return 관광지 검색 결과
     */
    public Page<TourSpotListDto> searchTourSpots(int page, int size, String sort, String keyword,
                                                 String areaCode, String sigunguCode, String classifiedTypeId) {

        log.debug("관광지 검색: keyword={}, page={}, size={}, area={}, sigungu={}, type={}",
                keyword, page, size, areaCode, sigunguCode, classifiedTypeId);

        // 페이징 및 정렬 설정 - title 필드는 sort_title로 변환
        Pageable pageable = createPageableWithFieldMapping(page, size, sort,
                Sort.by(Sort.Direction.DESC, "_score"),
                Map.of("title", "sort_title"));

        // 쿼리 빌더 생성 및 검색 조건 설정
        BoolQueryBuilder boolQuery = createTourSpotSearchQuery(keyword, areaCode, sigunguCode, classifiedTypeId);
        Query query = buildQuery(boolQuery, pageable);

        // 검색 실행
        SearchHits<TourSpots> searchHits = elasticsearchOperations.search(query, TourSpots.class);
        if (searchHits.isEmpty()) {
            log.debug("관광지 검색 결과 없음: keyword={}", keyword);
            return Page.empty(pageable);
        }

        // 결과 처리
        List<TourSpotListDto> dtoList = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .map(TourSpots::convertToListDto)
                .collect(Collectors.toList());

        return new PageImpl<>(dtoList, pageable, searchHits.getTotalHits());
    }

    /**
     * 관광지 검색 쿼리 생성
     */
    private BoolQueryBuilder createTourSpotSearchQuery(String keyword, String areaCode,
                                                       String sigunguCode, String classifiedTypeId) {
        BoolQueryBuilder boolQuery = boolQuery();

        boolean hasFilters = keyword != null || areaCode != null || sigunguCode != null || classifiedTypeId != null;

        if (!hasFilters) {
            boolQuery.must(QueryBuilders.matchAllQuery());
            return boolQuery;
        }

        // 키워드 검색 처리
        if (keyword != null && !keyword.isEmpty()) {
            BoolQueryBuilder keywordQuery = boolQuery()
                    .should(QueryBuilders.matchPhraseQuery("title", keyword).boost(PHRASE_MATCH_BOOST))
                    .should(QueryBuilders.multiMatchQuery(keyword, "title.ngram", "addr1.ngram").boost(PARTIAL_MATCH_BOOST))
                    .minimumShouldMatch(1);
            boolQuery.must(keywordQuery);
        }

        // 필터 조건 추가
        addLocationFilter(boolQuery, areaCode, sigunguCode);

        // 분류 타입 필터 추가
        if (classifiedTypeId != null) {
            boolQuery.filter(termQuery("classified_type_id", classifiedTypeId));
        }

        return boolQuery;
    }

    /**
     * 나의 다이어리 목록 조회 (비공개 포함)
     *
     * @param userId 사용자 ID
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 사용자의 다이어리 목록
     */
    public Page<DiarySearchListDto> getMyDiaryList(String userId, int page, int size) {
        log.debug("내 다이어리 목록 조회: userId={}, page={}, size={}", userId, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Member author = findMemberByUserId(userId);

        // 사용자의 모든 다이어리 조회 (공개/비공개 포함)
        BoolQueryBuilder boolQuery = boolQuery()
                .filter(termQuery("member_id", author.getId()));

        Query query = buildQuery(boolQuery, pageable);

        return executeDiarySearchForUser(query, pageable, author);
    }

    /**
     * 특정 사용자의 다이어리 목록 조회 (공개된 것만)
     *
     * @param userId 사용자 ID
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 사용자의 공개 다이어리 목록
     */
    public Page<DiarySearchListDto> getOtherUserDiaryList(String userId, int page, int size) {
        log.debug("타 사용자 다이어리 목록 조회: userId={}, page={}, size={}", userId, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Member author = findMemberByUserId(userId);

        // 사용자의 공개 다이어리만 조회
        BoolQueryBuilder boolQuery = boolQuery()
                .filter(termQuery("member_id", author.getId()))
                .filter(termQuery("is_public", true));

        Query query = buildQuery(boolQuery, pageable);

        return executeDiarySearchForUser(query, pageable, author);
    }

    /**
     * 북마크한 다이어리 목록 조회
     *
     * @param userId 사용자 ID
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 북마크한 다이어리 목록
     */
    public Page<DiarySearchListDto> getBookmarkedDiaries(String userId, int page, int size) {
        log.debug("북마크한 다이어리 목록 조회: userId={}, page={}, size={}", userId, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Member member = findMemberByUserId(userId);

        // 북마크한 다이어리 ID 목록 조회
        List<String> bookmarkedIds = getBookmarkedIdsByType(member, Type.DIARY);
        if (bookmarkedIds.isEmpty()) {
            return Page.empty(pageable);
        }

        // 북마크한 공개 다이어리만 조회
        BoolQueryBuilder boolQuery = boolQuery()
                .filter(termsQuery("diary_id", bookmarkedIds))
                .filter(termQuery("is_public", true));

        Query query = buildQuery(boolQuery, pageable);

        return executeDiarySearchForUser(query, pageable, member);
    }

    /**
     * 북마크한 관광지 목록 조회
     *
     * @param userId 사용자 ID
     * @param page 페이지 번호
     * @param size 페이지 크기
     * @return 북마크한 관광지 목록
     */
    public Page<TourSpotListDto> getBookmarkedTourSpots(String userId, int page, int size) {
        log.debug("북마크한 관광지 목록 조회: userId={}, page={}, size={}", userId, page, size);

        Pageable pageable = PageRequest.of(page, size);
        Member member = findMemberByUserId(userId);

        // 북마크한 관광지 ID 목록 조회
        List<String> bookmarkedIds = getBookmarkedIdsByType(member, Type.TOURSPOT);
        if (bookmarkedIds.isEmpty()) {
            return Page.empty(pageable);
        }

        // 북마크한 관광지 조회
        BoolQueryBuilder boolQuery = boolQuery()
                .filter(termsQuery("content_id", bookmarkedIds));

        Query query = buildQuery(boolQuery, pageable);

        SearchHits<TourSpots> searchHits = elasticsearchOperations.search(query, TourSpots.class);
        if (searchHits.isEmpty()) {
            return Page.empty(pageable);
        }

        List<TourSpotListDto> dtoList = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .map(TourSpots::convertToListDto)
                .collect(Collectors.toList());

        return new PageImpl<>(dtoList, pageable, searchHits.getTotalHits());
    }

    /**
     * 10개의 키워드를 받아 각 키워드당 10개 추천 반환
     *
     * @param keywords 키워드 목록
     * @return 키워드별 추천 관광지 목록
     */
    public Map<String, List<TourSpotListDto>> get10SpotsRecommend(List<String> keywords) {
        log.debug("키워드 기반 관광지 추천: keywords={}", keywords);

        Map<String, List<TourSpotListDto>> resultMap = new HashMap<>();
        Pageable pageable = PageRequest.of(0, RECOMMEND_COUNT);

        for (String keyword : keywords) {
            if (keyword == null || keyword.trim().isEmpty()) {
                resultMap.put(keyword, List.of());
                continue;
            }

            resultMap.put(keyword, getRecommendedSpotsByKeyword(keyword, pageable));
        }

        return resultMap;
    }

    /**
     * 키워드 기반 관광지 추천 목록 조회
     */
    private List<TourSpotListDto> getRecommendedSpotsByKeyword(String keyword, Pageable pageable) {
        BoolQueryBuilder boolQuery = boolQuery()
                .should(QueryBuilders.matchPhraseQuery("title", keyword))
                .minimumShouldMatch(1);

        Query query = buildQuery(boolQuery, pageable);

        SearchHits<TourSpots> searchHits = elasticsearchOperations.search(query, TourSpots.class);

        return searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .map(TourSpots::convertToSimpleDto)
                .collect(Collectors.toList());
    }

    /**
     * 특정 사용자에 대한 다이어리 검색 실행
     */
    private Page<DiarySearchListDto> executeDiarySearchForUser(Query query, Pageable pageable, Member member) {
        SearchHits<Diary> searchHits = elasticsearchOperations.search(query, Diary.class);
        if (searchHits.isEmpty()) {
            log.debug("사용자 다이어리 검색 결과 없음: memberId={}", member.getId());
            return Page.empty(pageable);
        }

        List<Diary> diaries = extractContentsFromHits(searchHits);
        Map<Long, Member> memberMap = Collections.singletonMap(member.getId(), member);
        List<DiarySearchListDto> dtoList = mapToDiaryDtoList(diaries, memberMap);

        return new PageImpl<>(dtoList, pageable, searchHits.getTotalHits());
    }

    /**
     * 사용자 ID로 회원 정보 조회
     */
    private Member findMemberByUserId(String userId) {
        return memberRepository.findByUserId(userId)
                .orElseThrow(() -> ResourceNotFoundException.memberNotFound(userId));
    }

    /**
     * 회원의 북마크 ID 목록 조회 (타입별)
     */
    private List<String> getBookmarkedIdsByType(Member member, Type type) {
        List<Bookmark> bookmarks = bookmarkRepository.findByMemberAndType(member, type);
        return bookmarks.stream()
                .map(Bookmark::getBookmarkedId)
                .collect(Collectors.toList());
    }

    /**
     * 검색 결과에서 컨텐츠 추출
     */
    private <T> List<T> extractContentsFromHits(SearchHits<T> searchHits) {
        return searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());
    }

    /**
     * 페이지네이션 및 정렬 객체 생성
     */
    private Pageable createPageable(int page, int size, String sort, Sort defaultSort) {
        if (sort == null || sort.isEmpty()) {
            return PageRequest.of(page, size, defaultSort);
        }

        String[] sortParts = sort.split(",");
        if (sortParts.length != 2) {
            return PageRequest.of(page, size, defaultSort);
        }

        String field = sortParts[0];
        Sort.Direction direction = Sort.Direction.fromString(sortParts[1]);

        return PageRequest.of(page, size, Sort.by(direction, field));
    }

    /**
     * 페이지네이션 및 정렬 객체 생성 (필드 매핑 적용)
     */
    private Pageable createPageableWithFieldMapping(int page, int size, String sort, Sort defaultSort, Map<String, String> fieldMapping) {
        if (sort == null || sort.isEmpty()) {
            return PageRequest.of(page, size, defaultSort);
        }

        String[] sortParts = sort.split(",");
        if (sortParts.length != 2) {
            return PageRequest.of(page, size, defaultSort);
        }

        String field = sortParts[0];
        Sort.Direction direction = Sort.Direction.fromString(sortParts[1]);

        // 필드 매핑 적용
        if (fieldMapping.containsKey(field)) {
            field = fieldMapping.get(field);
        }

        return PageRequest.of(page, size, Sort.by(direction, field));
    }

    /**
     * 쿼리 객체 생성
     */
    private Query buildQuery(BoolQueryBuilder boolQuery, Pageable pageable) {
        return new NativeSearchQueryBuilder()
                .withQuery(boolQuery)
                .withPageable(pageable)
                .build();
    }

    /**
     * 다이어리 리스트를 DTO로 변환
     */
    private List<DiarySearchListDto> mapToDiaryDtoList(List<Diary> diaries, Map<Long, Member> memberMap) {
        return diaries.stream()
                .map(diary -> {
                    Member author = memberMap.get(diary.getMemberId());
                    String plainContent = stripHtmlTags(diary.getContent());

                    return DiarySearchListDto.builder()
                            .diaryId(diary.getDiaryId())
                            .title(diary.getTitle())
                            .contentSummary(createContentSummary(plainContent))
                            .thumbnail(extractFirstImageSrc(diary.getContent()))
                            .writer(author.getNickname())
                            .writerImg(author.getImgPath())
                            .createdAt(diary.getCreatedTime())
                            .startDate(diary.getStartDate())
                            .endDate(diary.getEndDate())
                            .region(diary.getRegion())
                            .build();
                })
                .toList();
    }

    /**
     * 컨텐츠 요약 생성
     */
    private String createContentSummary(String content) {
        if (content == null || content.length() <= CONTENT_SUMMARY_MAX_LENGTH) {
            return content;
        }
        return content.substring(0, CONTENT_SUMMARY_MAX_LENGTH) + "...";
    }

    /**
     * 다이어리 멤버 맵 생성
     */
    private Map<Long, Member> getMemberMap(List<Diary> diaries) {
        List<Long> memberIdList = diaries.stream()
                .map(Diary::getMemberId)
                .distinct()
                .toList();

        return memberRepository.findByIdIn(memberIdList)
                .stream()
                .collect(Collectors.toMap(Member::getId, member -> member));
    }

    /**
     * HTML 태그 제거
     */
    private String stripHtmlTags(String content) {
        if (content == null) return "";
        return Jsoup.parse(content).text().trim();
    }

    /**
     * HTML에서 첫 번째 이미지 URL 추출
     */
    private String extractFirstImageSrc(String content) {
        if (content == null || !content.contains("<img")) {
            return null;
        }

        try {
            Document doc = Jsoup.parse(content);
            Element img = doc.selectFirst("img");
            return img != null ? img.attr("src") : null;
        } catch (Exception e) {
            log.warn("이미지 URL 추출 실패: {}", e.getMessage());
            return null;
        }
    }
}
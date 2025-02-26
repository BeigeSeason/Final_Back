package com.springboot.final_back.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.springboot.final_back.dto.TourSpotDetailDto;
import com.springboot.final_back.entity.elasticsearch.TourSpots;
import com.springboot.final_back.repository.TourSpotsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.document.Document;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.UpdateQuery;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
@Transactional
public class TourSpotService {
    private final TourSpotsRepository tourSpotsRepository;
    private final ElasticsearchOperations elasticsearchOperations;
    private final RestTemplate restTemplate;

    @Value("${tour.api.service-key1}")
    private String serviceKey1;

    @Value("${tour.api.service-key2}")
    private String serviceKey2;

    @Value("${tour.api.service-key3}")
    private String serviceKey3;

    private static final String BASE_URL = "https://apis.data.go.kr/B551011/KorService1";
    private static final String INDEX_NAME = "tour_spots";

    public TourSpotDetailDto getTourSpotDetail(String tourSpotId) {
        try {
            Optional<TourSpots> tourSpotOpt = tourSpotsRepository.findByContentId(tourSpotId);

            if (tourSpotOpt.isPresent()) {
                TourSpots tourSpot = tourSpotOpt.get();
                TourSpots.Detail detail = tourSpot.getDetail();

                if (detail != null) {
                    return convertToDto(tourSpot, detail);
                }

                TourSpotDetailDto detailDto = fetchDetailFromApi(tourSpotId, tourSpot.getContentTypeId());
                saveDetailToElasticsearch(tourSpotOpt.get().getId(), detailDto);
                return detailDto;
            } else {
                throw new RuntimeException("해당 관광지 데이터가 없습니다: " + tourSpotId);
            }
        } catch (Exception e) {
            log.error("상세 정보 조회 중 오류: {}", e.getMessage());
            throw new RuntimeException("상세 정보를 가져오지 못했습니다.");
        }
    }

    private TourSpotDetailDto fetchDetailFromApi(String contentId, String contentTypeId) {
        try {

            String commonUrl = BASE_URL + "/detailCommon1?MobileOS=ETC&MobileApp=Final_test&_type=json" +
                    "&contentId=" + contentId + "&defaultYN=Y&overviewYN=Y&serviceKey=" + serviceKey1;
            log.info("호출 URL (common): {}", commonUrl);

            URL url = new URL(commonUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Accept", "application/json");
            conn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/133.0.0.0 Safari/537.36");

            StringBuilder responseBody = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream(), "UTF-8"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    responseBody.append(line);
                }
            }

            ObjectMapper mapper = new ObjectMapper();
            Map<String, Object> commonResponse = mapper.readValue(responseBody.toString(), new TypeReference<>() {
            });
            Map<String, Object> commonItem = extractItem(commonResponse);

            // 2. detailImage1 호출
            String imageUrl = BASE_URL + "/detailImage1?MobileOS=ETC&MobileApp=Final_test&_type=json" +
                    "&contentId=" + contentId + "&subImageYN=Y&serviceKey=" + serviceKey2;
            log.info("호출 URL (image): {}", imageUrl);
            URL imageUrlObj = new URL(imageUrl);
            HttpURLConnection imageConn = (HttpURLConnection) imageUrlObj.openConnection();
            imageConn.setRequestMethod("GET");
            imageConn.setRequestProperty("Accept", "application/json");
            imageConn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/133.0.0.0 Safari/537.36");

            StringBuilder imageResponseBody = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(imageConn.getInputStream(), "UTF-8"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    imageResponseBody.append(line);
                }
            }
            Map<String, Object> imageResponse = mapper.readValue(imageResponseBody.toString(), new TypeReference<>() {
            });
            List<Map<String, Object>> imageItems = extractItems(imageResponse);

            // 3. detailIntro1 호출
            String introUrl = BASE_URL + "/detailIntro1?MobileOS=ETC&MobileApp=Final_test&_type=json" +
                    "&contentId=" + contentId + "&contentTypeId=" + contentTypeId + "&serviceKey=" + serviceKey3;
            log.info("호출 URL (intro): {}", introUrl);
            URL introUrlObj = new URL(introUrl);
            HttpURLConnection introConn = (HttpURLConnection) introUrlObj.openConnection();
            introConn.setRequestMethod("GET");
            introConn.setRequestProperty("Accept", "application/json");
            introConn.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/133.0.0.0 Safari/537.36");

            StringBuilder introResponseBody = new StringBuilder();
            try (BufferedReader br = new BufferedReader(new InputStreamReader(introConn.getInputStream(), "UTF-8"))) {
                String line;
                while ((line = br.readLine()) != null) {
                    introResponseBody.append(line);
                }
            }
            Map<String, Object> introResponse = mapper.readValue(introResponseBody.toString(), new TypeReference<>() {
            });
            Map<String, Object> introItem = extractItem(introResponse);

            return TourSpotDetailDto.builder()
                    .contentId(contentId)
                    .title((String) commonItem.getOrDefault("title", ""))
                    .addr1((String) commonItem.getOrDefault("addr1", ""))
                    .images(imageItems.stream()
                            .map(item -> (String) item.get("originimgurl"))
                            .collect(Collectors.toList()))
                    .overview((String) commonItem.getOrDefault("overview", ""))
                    .homepage((String) commonItem.getOrDefault("homepage", ""))
                    .useTime((String) introItem.getOrDefault("usetime", ""))
                    .parking((String) introItem.getOrDefault("parking", ""))
                    .contact((String) commonItem.getOrDefault("tel", ""))
                    .mapX(Float.valueOf(String.valueOf(commonItem.getOrDefault("mapx", "0"))))
                    .mapY(Float.valueOf(String.valueOf(commonItem.getOrDefault("mapy", "0"))))
                    .build();
        } catch (Exception e) {
            log.error("API 호출 중 오류 - contentId: {}: {}", contentId, e.getMessage());
            throw new RuntimeException("API 데이터를 가져오지 못했습니다.");
        }
    }

    private void saveDetailToElasticsearch(String contentId, TourSpotDetailDto detailDto) {
        UpdateQuery updateQuery = UpdateQuery.builder(contentId)
                .withDocument(Document.from(Map.of(
                        "detail", Map.of(
                                "images", detailDto.getImages(),
                                "overview", detailDto.getOverview(),
                                "homepage", detailDto.getHomepage(),
                                "useTime", detailDto.getUseTime(),
                                "parking", detailDto.getParking()
                        )
                )))
                .build();
        elasticsearchOperations.update(updateQuery, IndexCoordinates.of(INDEX_NAME));
        log.info("관광지 {} 상세 정보 저장 완료", contentId);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> extractItem(Map<String, Object> response) {
        try {
            Map<String, Object> body = (Map<String, Object>) response.get("response");
            Map<String, Object> items = (Map<String, Object>) ((Map<String, Object>) body.get("body")).get("items");
            List<Map<String, Object>> itemList = (List<Map<String, Object>>) items.get("item");
            return itemList.get(0);
        } catch (Exception e) {
            log.error("아이템 추출 실패: {}", e.getMessage());
            return Collections.emptyMap();
        }
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> extractItems(Map<String, Object> response) {
        try {
            Map<String, Object> body = (Map<String, Object>) response.get("response");
            Map<String, Object> items = (Map<String, Object>) ((Map<String, Object>) body.get("body")).get("items");
            return (List<Map<String, Object>>) items.get("item");
        } catch (Exception e) {
            log.error("이미지 목록 추출 실패: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    private TourSpotDetailDto convertToDto(TourSpots tourSpot, TourSpots.Detail detail) {
        return TourSpotDetailDto.builder()
                .contentId(tourSpot.getContentId())
                .title(tourSpot.getTitle())
                .addr1(tourSpot.getAddr1())
                .images(detail.getImages())
                .overview(detail.getOverview())
                .homepage(detail.getHomepage())
                .useTime(detail.getUseTime())
                .parking(detail.getParking())
                .contact(tourSpot.getTel())
                .mapX(tourSpot.getMapX())
                .mapY(tourSpot.getMapY())
                .build();
    }
}
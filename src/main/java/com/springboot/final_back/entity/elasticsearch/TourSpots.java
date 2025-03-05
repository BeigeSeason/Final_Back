package com.springboot.final_back.entity.elasticsearch;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.springboot.final_back.dto.search.TourSpotListDto;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.InnerField;
import org.springframework.data.elasticsearch.annotations.MultiField;



import java.time.LocalDateTime;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@Document(indexName = "tour_spots")
public class TourSpots {
    // 관광지 아이디
    @Id
    private String id;

    @Field(name ="content_id" , type = FieldType.Keyword)
    @JsonProperty("content_id")
    private String contentId;

    // 주소
    // N-gram 분석은 텍스트를 N-gram (N글자씩 묶음) 단위로 분리하여 인덱싱하는 방식
    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "nori_analyzer_simple"),
            otherFields = {
                    @InnerField(type = FieldType.Text, analyzer = "nori_ngram_analyzer", suffix = "ngram")
            }
    )
    @JsonProperty("addr1")
    private String addr1;

    // 상세주소
    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "nori_analyzer_simple"),
            otherFields = {
                    @InnerField(type = FieldType.Text, analyzer = "nori_ngram_analyzer", suffix = "ngram")
            }
    )
    @JsonProperty("addr2")
    private String addr2;

    // 제목
    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "nori_analyzer_simple"),
            otherFields = {
                    @InnerField(type = FieldType.Text, analyzer = "nori_ngram_analyzer", suffix = "ngram")
            }
    )
    @JsonProperty("title")
    private String title;

    // 지역코드(시도)
    @Field(type = FieldType.Keyword, name = "area_code")
    @JsonProperty("area_code")
    private String areaCode;

    // 지역코드(시군구)
    @Field(type = FieldType.Keyword, name = "sigungu_code")
    @JsonProperty("sigungu_code")
    private String sigunguCode;

    // 타입 아이디
    @Field(type = FieldType.Keyword, name = "content_type_id")
    @JsonProperty("content_type_id")
    private String contentTypeId;

    // 대분류
    @Field(type = FieldType.Keyword)
    @JsonProperty("cat1")
    private String cat1;

    // 중분류
    @Field(type = FieldType.Keyword)
    @JsonProperty("cat2")
    private String cat2;

    // 소분류
    @Field(type = FieldType.Keyword)
    @JsonProperty("cat3")
    private String cat3;

    // 등록일
    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second, name = "created_time")
    @JsonProperty("created_time")
    private LocalDateTime createdTime;

    // 수정일
    @Field(type = FieldType.Date, format = DateFormat.date_hour_minute_second, name = "modified_time")
    @JsonProperty("modified_time")
    private LocalDateTime modifiedTime;

    // 이미지 1
    @Field(type = FieldType.Keyword, name = "first_image")
    @JsonProperty("first_image")
    private String firstImage;

    // 이미지 2
    @Field(type = FieldType.Keyword, name = "first_image2")
    @JsonProperty("first_image2")
    private String firstImage2;

    // 전화번호
    @Field(type = FieldType.Keyword, name = "tel")
    @JsonProperty("tel")
    private String tel;

    // 경도
    @Field(type = FieldType.Float, name = "map_x")
    @JsonProperty("map_x")
    private Float mapX;

    // 위도
    @Field(type = FieldType.Float, name = "map_y")
    @JsonProperty("map_y")
    private Float mapY;

    // 지도 레벨 (시간 남으면 제거)
    @Field(type = FieldType.Float, name = "m_level")
    @JsonProperty("m_level")
    private Float mLevel;

    // 중첩 객체 관리를 위해 Nested 설정
    @Field(type = FieldType.Nested)
    @JsonProperty("detail")
    private Detail detail;

    @Field(type = FieldType.Float, name = "review_count")
    @JsonProperty("review_count")
    private float reviewCount;

    @Field(type = FieldType.Double)
    @JsonProperty("rating")
    private double rating;

    @Field(type = FieldType.Float, name = "bookmark_count")
    @JsonProperty("bookmark_count")
    private float bookmarkCount;

    // detail 필드는 여기서밖에 사용하지 않기 때문에 엔티티 내부에 정의함
    @Data
    public static class Detail{
        @Field(type = FieldType.Keyword)
        @JsonProperty("images")
        private List<String> images;

        @Field(type = FieldType.Keyword, name = "info_center")
        @JsonProperty("info_center")
        private String infoCenter;

        @Field(type = FieldType.Keyword)
        @JsonProperty("overview")
        private String overview;

        @Field(type = FieldType.Keyword, name = "use_time")
        @JsonProperty("use_time")
        private String useTime;

        @Field(type = FieldType.Keyword)
        @JsonProperty("parking")
        private String parking;

        @Field(type = FieldType.Keyword)
        @JsonProperty("homepage")
        private String homepage;
    }

    public TourSpotListDto convertToListDto(){
        return TourSpotListDto.builder()
                .spotId(contentId)
                .title(title)
                .addr(addr1)
                .thumbnail(firstImage)
                .build();
    }
}
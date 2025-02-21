package com.springboot.final_back.entity.elasticsearch;

import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.InnerField;
import org.springframework.data.elasticsearch.annotations.MultiField;


import javax.persistence.Id;
import java.time.LocalDateTime;

@Data
@Document(indexName = "tour_spots")
public class TourSpots {
    @Id
    @Field(name ="contentid" , type = FieldType.Keyword)
    private String contentId;

    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "nori_analyzer_with_stopwords"),
            otherFields = {
                    @InnerField(type = FieldType.Text, analyzer = "nori_ngram_analyzer", suffix = "ngram")
            }
    )
    private String addr1;

    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "nori_analyzer_with_stopwords"),
            otherFields = {
                    @InnerField(type = FieldType.Text, analyzer = "nori_ngram_analyzer", suffix = "ngram")
            }
    )
    private String addr2;

    @MultiField(
            mainField = @Field(type = FieldType.Text, analyzer = "nori_analyzer_with_stopwords"),
            otherFields = {
                    @InnerField(type = FieldType.Text, analyzer = "nori_ngram_analyzer", suffix = "ngram")
            }
    )
    private String title;

    @Field(type = FieldType.Keyword)
    private String areaCode;

    @Field(type = FieldType.Keyword)
    private String sigunguCode;

    @Field(type = FieldType.Keyword)
    private String zipcode;

    @Field(type = FieldType.Keyword)
    private String contentTypeId;

    @Field(type = FieldType.Keyword)
    private String cat1;

    @Field(type = FieldType.Keyword)
    private String cat2;

    @Field(type = FieldType.Keyword)
    private String cat3;

    @Field(type = FieldType.Date, format = DateFormat.custom, pattern = "yyyyMMddHHmmss")
    private LocalDateTime createdTime;

    @Field(type = FieldType.Date, format = DateFormat.basic_date, pattern = "yyyyMMddHHmmss")
    private LocalDateTime modifiedTime;

    @Field(type = FieldType.Keyword)
    private String firstImage;

    @Field(type = FieldType.Keyword)
    private String firstImage2;

    @Field(type = FieldType.Keyword)
    private String bookTour;

    @Field(type = FieldType.Keyword)
    private String cpyrhtDivCd;

    @Field(type = FieldType.Keyword)
    private String tel;

    @Field(type = FieldType.Float)
    private Float mapX;

    @Field(type = FieldType.Float)
    private Float mapY;

    @Field(type = FieldType.Float)
    private Float mLevel;
}
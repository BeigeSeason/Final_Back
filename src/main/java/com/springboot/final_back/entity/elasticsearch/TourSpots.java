package com.springboot.final_back.entity.elasticsearch;

import lombok.Data;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.InnerField;
import org.springframework.data.elasticsearch.annotations.MultiField;


import javax.persistence.Id;

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
    private String areacode;

    @Field(type = FieldType.Keyword)
    private String sigungucode;

    @Field(type = FieldType.Keyword)
    private String zipcode;

    @Field(type = FieldType.Keyword)
    private String contenttypeid;

    @Field(type = FieldType.Keyword)
    private String cat1;

    @Field(type = FieldType.Keyword)
    private String cat2;

    @Field(type = FieldType.Keyword)
    private String cat3;

    @Field(type = FieldType.Date, format = DateFormat.custom, pattern = "yyyyMMddHHmmss")
    private String createdtime;

    @Field(type = FieldType.Date, format = DateFormat.basic_date, pattern = "yyyyMMddHHmmss")
    private String modifiedtime;

    @Field(type = FieldType.Keyword)
    private String firstimage;

    @Field(type = FieldType.Keyword)
    private String firstimage2;

    @Field(type = FieldType.Keyword)
    private String booktour;

    @Field(type = FieldType.Keyword)
    private String cpyrhtDivCd;

    @Field(type = FieldType.Keyword)
    private String tel;

    @Field(type = FieldType.Float)
    private Float mapx;

    @Field(type = FieldType.Float)
    private Float mapy;

    @Field(type = FieldType.Float)
    private Float mlevel;
}
package com.thundax.kuzhambu.discovery.infra.client;

import java.time.Instant;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "discovery-search")
public class DiscoverySearchDocument {

    @Id
    private String documentId;

    @Field(type = FieldType.Keyword)
    private String contentDomain;

    @Field(type = FieldType.Keyword)
    private String contentType;

    @Field(type = FieldType.Keyword)
    private String contentId;

    @Field(type = FieldType.Keyword)
    private String contentVersionId;

    @Field(type = FieldType.Integer)
    private Integer contentVersionNo;

    @Field(type = FieldType.Keyword)
    private String knowledgeBase;

    @Field(type = FieldType.Keyword)
    private String categoryCode;

    @Field(type = FieldType.Text)
    private String categoryName;

    @Field(type = FieldType.Keyword)
    private String volumeId;

    @Field(type = FieldType.Text)
    private String volumeTitle;

    @Field(type = FieldType.Text)
    private String title;

    @Field(type = FieldType.Text)
    private String summary;

    @Field(type = FieldType.Text)
    private String bodyText;

    @Field(type = FieldType.Text)
    private List<String> textSegments;

    @Field(type = FieldType.Keyword)
    private List<String> tagNames;

    @Field(type = FieldType.Keyword)
    private String publicationStatus;

    @Field(type = FieldType.Integer)
    private Integer sourceVersionNo;

    @Field(type = FieldType.Date, format = DateFormat.date_time)
    private Instant publishedAt;

    @Field(type = FieldType.Date, format = DateFormat.date_time)
    private Instant updatedAt;

    @Field(type = FieldType.Boolean)
    private Boolean deleted;

    @Field(type = FieldType.Date, format = DateFormat.date_time)
    private Instant deletedAt;

    @Field(type = FieldType.Keyword)
    private String sourcePath;

    public DiscoverySearchDocument(
            String documentId,
            String contentDomain,
            String contentType,
            String contentId,
            String knowledgeBase,
            String categoryCode,
            String categoryName,
            String title,
            String summary,
            String bodyText,
            List<String> tagNames,
            Integer sourceVersionNo,
            Instant publishedAt,
            Instant updatedAt,
            Boolean deleted,
            Instant deletedAt,
            String sourcePath) {
        this.documentId = documentId;
        this.contentDomain = contentDomain;
        this.contentType = contentType;
        this.contentId = contentId;
        this.knowledgeBase = knowledgeBase;
        this.categoryCode = categoryCode;
        this.categoryName = categoryName;
        this.title = title;
        this.summary = summary;
        this.bodyText = bodyText;
        this.tagNames = tagNames;
        this.sourceVersionNo = sourceVersionNo;
        this.publishedAt = publishedAt;
        this.updatedAt = updatedAt;
        this.deleted = deleted;
        this.deletedAt = deletedAt;
        this.sourcePath = sourcePath;
    }
}

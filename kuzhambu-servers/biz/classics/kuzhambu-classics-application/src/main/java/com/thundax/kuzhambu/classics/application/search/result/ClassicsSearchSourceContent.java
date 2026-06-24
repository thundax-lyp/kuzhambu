package com.thundax.kuzhambu.classics.application.search.result;

import java.util.Date;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClassicsSearchSourceContent {
    private String contentType;
    private String contentId;
    private String knowledgeBase;
    private String categoryCode;
    private String categoryName;
    private String title;
    private String summary;
    private List<String> textSegments;
    private List<String> tagNames;
    private String status;
    private String visibility;
    private Integer currentVersionNo;
    private Date publishedAt;
    private Date updatedAt;

    public ClassicsSearchSourceContent(
            String contentType,
            String contentId,
            String knowledgeBase,
            String categoryCode,
            String categoryName,
            String title,
            String summary,
            List<String> textSegments,
            List<String> tagNames,
            String status,
            String visibility,
            Date publishedAt,
            Date updatedAt) {
        this(
                contentType,
                contentId,
                knowledgeBase,
                categoryCode,
                categoryName,
                title,
                summary,
                textSegments,
                tagNames,
                status,
                visibility,
                null,
                publishedAt,
                updatedAt);
    }
}

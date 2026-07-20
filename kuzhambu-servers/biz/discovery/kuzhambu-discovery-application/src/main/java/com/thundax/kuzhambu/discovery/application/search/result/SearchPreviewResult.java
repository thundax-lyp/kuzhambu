package com.thundax.kuzhambu.discovery.application.search.result;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SearchPreviewResult {
    private String contentDomain;
    private String contentType;
    private String contentId;
    private String knowledgeBase;
    private String categoryCode;
    private String categoryName;
    private String title;
    private String summary;
    private String bodyText;
    private List<String> tagNames;
    private String contentStatus;
    private String visibility;
    private Integer sourceVersionNo;
    private Long publishedAt;
    private Long updatedAt;
    private String targetPath;
}

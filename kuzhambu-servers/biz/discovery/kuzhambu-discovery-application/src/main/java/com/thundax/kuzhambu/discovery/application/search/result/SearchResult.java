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
public class SearchResult {
    private String contentDomain;
    private String contentType;
    private String contentId;
    private String knowledgeBase;
    private String categoryCode;
    private String title;
    private String summary;
    private String highlightText;
    private List<String> tagNames;
    private String contentStatus;
    private String visibility;
    private Long updatedAt;
    private int resultRank;
    private int groupRank;
    private String targetPath;

    public SearchResult(
            String contentDomain,
            String contentType,
            String contentId,
            String title,
            String summary,
            String highlightText,
            int resultRank,
            int groupRank,
            String targetPath) {
        this(
                contentDomain,
                contentType,
                contentId,
                null,
                null,
                title,
                summary,
                highlightText,
                null,
                null,
                null,
                null,
                resultRank,
                groupRank,
                targetPath);
    }
}

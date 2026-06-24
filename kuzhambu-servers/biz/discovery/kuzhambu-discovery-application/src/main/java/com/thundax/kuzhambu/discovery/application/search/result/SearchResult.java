package com.thundax.kuzhambu.discovery.application.search.result;

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
    private String title;
    private String summary;
    private String highlightText;
    private int resultRank;
    private int groupRank;
    private String targetPath;
}

package com.thundax.kuzhambu.discovery.application.search.query;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SearchPublicationCandidatePageQuery {

    private final String contentType;
    private final String categoryId;
    private final String volumeId;
    private final String keyword;
    private final Integer pageNo;
    private final Integer pageSize;
}

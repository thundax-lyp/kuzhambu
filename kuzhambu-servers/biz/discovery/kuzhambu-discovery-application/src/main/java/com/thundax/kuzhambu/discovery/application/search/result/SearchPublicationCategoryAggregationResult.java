package com.thundax.kuzhambu.discovery.application.search.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SearchPublicationCategoryAggregationResult {

    private final String categoryId;
    private final long readyEntryCount;
    private final String representativeContentId;
}

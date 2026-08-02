package com.thundax.kuzhambu.discovery.application.search.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SearchPublicationCandidateResult {

    private final String contentType;
    private final String contentId;
    private final String categoryId;
    private final String volumeId;
}

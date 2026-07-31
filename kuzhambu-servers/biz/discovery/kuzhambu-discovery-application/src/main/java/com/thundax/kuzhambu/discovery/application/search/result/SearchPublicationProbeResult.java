package com.thundax.kuzhambu.discovery.application.search.result;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SearchPublicationProbeResult {

    private final boolean present;
    private final String publicationStatus;
    private final Boolean deleted;
    private final String contentVersionId;
    private final Integer contentVersionNo;

    public static SearchPublicationProbeResult missing() {
        return new SearchPublicationProbeResult(false, null, null, null, null);
    }
}

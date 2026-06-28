package com.thundax.kuzhambu.knowledge.facade.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class KnowledgeTagHintFacadeResponse {

    private final String term;
    private final String normalizedTerm;
    private final String matchedTagName;
    private final String matchedAliasName;
    private final Long contentRefCount;
}

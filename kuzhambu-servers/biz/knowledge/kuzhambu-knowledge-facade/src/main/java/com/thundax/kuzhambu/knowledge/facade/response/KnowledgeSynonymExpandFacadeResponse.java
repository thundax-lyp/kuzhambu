package com.thundax.kuzhambu.knowledge.facade.response;

import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class KnowledgeSynonymExpandFacadeResponse {

    private final String term;
    private final String normalizedTerm;
    private final List<String> expandedTerms;
}

package com.thundax.kuzhambu.knowledge.facade.response;

import com.thundax.kuzhambu.knowledge.facade.dto.KnowledgeSynonymMatchFacadeDto;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class KnowledgeSynonymQueryFacadeResponse {

    private final String term;
    private final String normalizedTerm;
    private final String direction;
    private final int limit;
    private final List<KnowledgeSynonymMatchFacadeDto> matches;
    private final List<String> expandedTerms;
}

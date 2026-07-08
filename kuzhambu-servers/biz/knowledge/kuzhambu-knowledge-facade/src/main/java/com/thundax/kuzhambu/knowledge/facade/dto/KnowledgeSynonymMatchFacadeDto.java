package com.thundax.kuzhambu.knowledge.facade.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class KnowledgeSynonymMatchFacadeDto {

    private final String sourceTerm;
    private final String targetTerm;
    private final String matchedTerm;
    private final String expandedTerm;
    private final String direction;
}

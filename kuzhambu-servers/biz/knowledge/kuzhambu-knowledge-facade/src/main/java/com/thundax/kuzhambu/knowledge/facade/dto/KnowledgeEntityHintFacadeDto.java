package com.thundax.kuzhambu.knowledge.facade.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class KnowledgeEntityHintFacadeDto {

    private final String term;
    private final String normalizedTerm;
    private final String entityName;
    private final String entityType;
    private final Long contentRefCount;
}

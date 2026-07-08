package com.thundax.kuzhambu.knowledge.facade.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class KnowledgeSynonymQueryFacadeRequest {

    private final String term;
    private final String direction;
    private final Integer limit;
}

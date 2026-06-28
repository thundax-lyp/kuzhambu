package com.thundax.kuzhambu.knowledge.facade.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class KnowledgeTopTagFacadeDto {

    private final String tagName;
    private final Long contentRefCount;
}

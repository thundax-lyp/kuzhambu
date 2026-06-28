package com.thundax.kuzhambu.knowledge.facade.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class KnowledgeTagFacadeResponse {

    private final Long tagId;
    private final String tagName;
}

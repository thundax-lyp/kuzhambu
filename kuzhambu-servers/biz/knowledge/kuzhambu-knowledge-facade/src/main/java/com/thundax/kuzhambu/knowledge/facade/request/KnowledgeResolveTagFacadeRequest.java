package com.thundax.kuzhambu.knowledge.facade.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class KnowledgeResolveTagFacadeRequest {

    private final String tagName;
}

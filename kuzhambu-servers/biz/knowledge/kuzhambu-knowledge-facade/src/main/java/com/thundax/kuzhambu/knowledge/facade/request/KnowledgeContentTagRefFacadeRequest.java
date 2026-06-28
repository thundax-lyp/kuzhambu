package com.thundax.kuzhambu.knowledge.facade.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class KnowledgeContentTagRefFacadeRequest {

    private final Long tagId;
    private final String contentType;
    private final Long contentId;
    private final String contentTitle;
    private final String tagSource;
}

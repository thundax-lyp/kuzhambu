package com.thundax.kuzhambu.classics.facade.request;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ClassicsPublicContentFacadeRequest {

    private final String contentType;
    private final String contentId;

    @Builder
    private ClassicsPublicContentFacadeRequest(String contentType, String contentId) {
        this.contentType = contentType;
        this.contentId = contentId;
    }
}

package com.thundax.kuzhambu.classics.facade.request;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ClassicsPublicContentFacadeRequest {

    private final String contentType;
    private final String contentId;
}

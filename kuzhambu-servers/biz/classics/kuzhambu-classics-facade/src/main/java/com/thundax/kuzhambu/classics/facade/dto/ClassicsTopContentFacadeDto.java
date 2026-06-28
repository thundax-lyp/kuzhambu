package com.thundax.kuzhambu.classics.facade.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ClassicsTopContentFacadeDto {

    private final Long contentId;
    private final String contentType;
    private final String title;
    private final Long visitCount;

    @Builder
    private ClassicsTopContentFacadeDto(Long contentId, String contentType, String title, Long visitCount) {
        this.contentId = contentId;
        this.contentType = contentType;
        this.title = title;
        this.visitCount = visitCount;
    }
}

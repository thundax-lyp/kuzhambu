package com.thundax.kuzhambu.classics.facade.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ClassicsTopContentFacadeDto {

    private final Long contentId;
    private final String contentType;
    private final String title;
    private final Long visitCount;
}

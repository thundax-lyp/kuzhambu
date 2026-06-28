package com.thundax.kuzhambu.classics.facade.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ClassicsContentGrowthPointFacadeDto {

    private final String bucket;
    private final Long createdCount;
}

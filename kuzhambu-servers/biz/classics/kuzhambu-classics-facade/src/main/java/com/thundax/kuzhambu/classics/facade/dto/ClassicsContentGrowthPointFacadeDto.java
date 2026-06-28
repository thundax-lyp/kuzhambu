package com.thundax.kuzhambu.classics.facade.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class ClassicsContentGrowthPointFacadeDto {

    private final String bucket;
    private final Long createdCount;

    @Builder
    private ClassicsContentGrowthPointFacadeDto(String bucket, Long createdCount) {
        this.bucket = bucket;
        this.createdCount = createdCount;
    }
}

package com.thundax.kuzhambu.knowledge.facade.dto;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class KnowledgeCategoryDistributionFacadeDto {

    private final String categoryName;
    private final Long tagCount;
}

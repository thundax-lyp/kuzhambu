package com.thundax.kuzhambu.knowledge.facade.response;

import com.thundax.kuzhambu.knowledge.facade.dto.KnowledgeCategoryDistributionFacadeDto;
import com.thundax.kuzhambu.knowledge.facade.dto.KnowledgeMonthlyNewTagFacadeDto;
import com.thundax.kuzhambu.knowledge.facade.dto.KnowledgeTopTagFacadeDto;
import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class KnowledgeSummaryFacadeResponse {

    private final Date periodStart;
    private final Date periodEnd;
    private final BigDecimal tagCoverageRate;
    private final List<KnowledgeTopTagFacadeDto> topTags;
    private final List<KnowledgeCategoryDistributionFacadeDto> categoryDistributions;
    private final List<KnowledgeMonthlyNewTagFacadeDto> monthlyNewTags;
}

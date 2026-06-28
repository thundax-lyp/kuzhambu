package com.thundax.kuzhambu.ai.facade.dto;

import java.math.BigDecimal;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class AiUsageSnapshotFacadeDto {

    private final Integer promptTokens;
    private final Integer completionTokens;
    private final Integer totalTokens;
    private final Integer latencyMs;
    private final BigDecimal costAmount;
    private final String currency;
}

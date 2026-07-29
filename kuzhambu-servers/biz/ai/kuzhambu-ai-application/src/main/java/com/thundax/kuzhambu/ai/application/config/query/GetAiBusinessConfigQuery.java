package com.thundax.kuzhambu.ai.application.config.query;

import com.thundax.kuzhambu.ai.domain.config.model.valueobject.AiBusinessConfigId;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GetAiBusinessConfigQuery {

    private final AiBusinessConfigId businessConfigId;
}

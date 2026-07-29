package com.thundax.kuzhambu.ai.application.config.query;

import com.thundax.kuzhambu.ai.domain.config.model.enums.AiApiSource;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ListAiModelsQuery {

    private final AiApiSource apiSource;
    private final Boolean enabled;
}

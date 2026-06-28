package com.thundax.kuzhambu.knowledge.facade.response;

import com.thundax.kuzhambu.knowledge.facade.dto.KnowledgeEntityHintFacadeDto;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class KnowledgeEntityHintsFacadeResponse {

    private final List<KnowledgeEntityHintFacadeDto> entityHints;
}

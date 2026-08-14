package com.thundax.kuzhambu.knowledge.application.graph.support;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.thundax.kuzhambu.ai.facade.dto.AiCandidateFacadeDto;
import com.thundax.kuzhambu.ai.facade.response.AiBatchJobFacadeResponse;
import org.junit.jupiter.api.Test;

class GraphApplicationAssemblerTest {

    @Test
    void extractionResultShouldExposeCandidateIdFromTheBatchCandidate() {
        AiBatchJobFacadeResponse batch = AiBatchJobFacadeResponse.builder()
                .batchId(88L)
                .contentType("SANCAI_ENTRY")
                .contentId(1001L)
                .status("SUCCEEDED")
                .build();
        AiCandidateFacadeDto candidate =
                AiCandidateFacadeDto.builder().candidateId(902L).build();

        var result = GraphApplicationAssembler.toExtractionResult(batch, candidate);

        assertEquals(88L, result.batchJobId());
        assertEquals(902L, result.candidateId());
    }
}

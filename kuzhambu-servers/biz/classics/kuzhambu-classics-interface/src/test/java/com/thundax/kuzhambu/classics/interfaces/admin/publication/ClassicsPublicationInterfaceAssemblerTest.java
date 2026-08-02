package com.thundax.kuzhambu.classics.interfaces.admin.publication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.thundax.kuzhambu.classics.application.publication.result.ClassicsPublicationCreateResult;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentId;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationJobType;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationLifecycleStatus;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationTransitionStatus;
import com.thundax.kuzhambu.classics.domain.publication.model.valueobject.ClassicsPublicationJobId;
import com.thundax.kuzhambu.classics.interfaces.admin.publication.assembler.ClassicsPublicationInterfaceAssembler;
import com.thundax.kuzhambu.classics.interfaces.admin.publication.controller.request.ClassicsPublicationBatchActionRequest;
import java.util.List;
import org.junit.jupiter.api.Test;

class ClassicsPublicationInterfaceAssemblerTest {

    @Test
    void shouldDeduplicateBatchIdsInRequestOrder() {
        var commands = ClassicsPublicationInterfaceAssembler.toCommands(
                new ClassicsPublicationBatchActionRequest(List.of(3L, 1L, 3L, 2L)),
                ClassicsContentType.SANCAI_ENTRY,
                ClassicsPublicationJobType.PUBLISH);

        assertEquals(
                List.of(3L, 1L, 2L),
                commands.stream().map(command -> command.contentId().value()).toList());
    }

    @Test
    void shouldSummarizeAcceptedAndRejectedBatchItems() {
        var response = ClassicsPublicationInterfaceAssembler.toBatchResponse(List.of(
                ClassicsPublicationCreateResult.success(
                        ClassicsContentType.MING_CUSTOMS,
                        new ClassicsContentId(8L),
                        new ClassicsPublicationJobId(18L),
                        ClassicsPublicationLifecycleStatus.OFFLINE,
                        ClassicsPublicationTransitionStatus.PUBLISHING),
                ClassicsPublicationCreateResult.failure(
                        ClassicsContentType.MING_CUSTOMS, new ClassicsContentId(9L), "INVALID_LIFECYCLE")));

        assertEquals(1, response.acceptedCount());
        assertEquals(1, response.rejectedCount());
        assertTrue(response.items().get(0).accepted());
        assertEquals(18L, response.items().get(0).jobId());
        assertFalse(response.items().get(1).accepted());
        assertEquals("INVALID_LIFECYCLE", response.items().get(1).reason());
    }
}

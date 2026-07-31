package com.thundax.kuzhambu.classics.application.publication;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import com.thundax.kuzhambu.classics.application.publication.service.impl.ClassicsPublicationStateMachine;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationJobStatus;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationJobType;
import java.util.Map;
import org.junit.jupiter.api.Test;

class ClassicsPublicationStateMachineTest {

    @Test
    void shouldReturnEveryPublishStep() {
        Map<ClassicsPublicationJobStatus, ClassicsPublicationJobStatus> expected = Map.of(
                ClassicsPublicationJobStatus.QUEUED, ClassicsPublicationJobStatus.SNAPSHOT_READY,
                ClassicsPublicationJobStatus.SNAPSHOT_READY, ClassicsPublicationJobStatus.ES_PREPARED,
                ClassicsPublicationJobStatus.ES_PREPARED, ClassicsPublicationJobStatus.FASTGPT_PREPARED,
                ClassicsPublicationJobStatus.FASTGPT_PREPARED, ClassicsPublicationJobStatus.ES_READY,
                ClassicsPublicationJobStatus.ES_READY, ClassicsPublicationJobStatus.FASTGPT_READY,
                ClassicsPublicationJobStatus.FASTGPT_READY, ClassicsPublicationJobStatus.CONTENT_COMMITTED);

        for (ClassicsPublicationJobStatus status : ClassicsPublicationJobStatus.values()) {
            assertEquals(
                    expected.get(status),
                    ClassicsPublicationStateMachine.nextStep(ClassicsPublicationJobType.PUBLISH, status));
        }
    }

    @Test
    void shouldReturnEveryOfflineStep() {
        Map<ClassicsPublicationJobStatus, ClassicsPublicationJobStatus> expected = Map.of(
                ClassicsPublicationJobStatus.QUEUED, ClassicsPublicationJobStatus.ES_DISABLED,
                ClassicsPublicationJobStatus.ES_DISABLED, ClassicsPublicationJobStatus.FASTGPT_DISABLED,
                ClassicsPublicationJobStatus.FASTGPT_DISABLED, ClassicsPublicationJobStatus.CONTENT_COMMITTED);

        for (ClassicsPublicationJobStatus status : ClassicsPublicationJobStatus.values()) {
            assertEquals(
                    expected.get(status),
                    ClassicsPublicationStateMachine.nextStep(ClassicsPublicationJobType.OFFLINE, status));
        }
    }

    @Test
    void shouldRejectIncompleteState() {
        assertNull(ClassicsPublicationStateMachine.nextStep(null, ClassicsPublicationJobStatus.QUEUED));
        assertNull(ClassicsPublicationStateMachine.nextStep(ClassicsPublicationJobType.PUBLISH, null));
    }
}

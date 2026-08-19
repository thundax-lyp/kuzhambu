package com.thundax.kuzhambu.ai.application.scenario.service.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.annotation.Scheduled;

class KnowledgeGraphExtractionTaskApplicationServiceImplTest {

    @Test
    void orphanedGraphJobsShouldExpireAfterTenMinutesAndBeCheckedEveryFiveMinutes()
            throws ReflectiveOperationException {
        Field timeout =
                KnowledgeGraphExtractionTaskApplicationServiceImpl.class.getDeclaredField("ORPHANED_TASK_TIMEOUT");
        timeout.setAccessible(true);
        Method expiryMethod = KnowledgeGraphExtractionTaskApplicationServiceImpl.class.getDeclaredMethod(
                "expireOrphanedRunningGraphJobs");
        Scheduled scheduled = expiryMethod.getAnnotation(Scheduled.class);

        assertThat(timeout.get(null)).isEqualTo(Duration.ofSeconds(600L));
        assertThat(scheduled.fixedDelayString())
                .isEqualTo("${kuzhambu.ai.knowledge-graph.orphaned-task-expiry.fixed-delay-ms:300000}");
    }
}

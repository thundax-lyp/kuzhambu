package com.thundax.kuzhambu.classics.application.cleanup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.classics.application.cleanup.service.ClassicsCleanupApplicationService.CleanupTarget;
import com.thundax.kuzhambu.classics.application.cleanup.service.impl.ClassicsCleanupApplicationServiceImpl;
import com.thundax.kuzhambu.classics.domain.content.repository.ClassicsContentRepository;
import com.thundax.kuzhambu.classics.domain.sancai.codec.SancaiEntryDraftIdCodec;
import com.thundax.kuzhambu.classics.domain.sancai.repository.SancaiAssetRepository;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;

class ClassicsCleanupApplicationServiceImplTest {

    @Test
    void listTargetsShouldApplyRetentionDaysAndLimitToExpiredDrafts() {
        SancaiAssetRepository sancaiAssetRepository = mock(SancaiAssetRepository.class);
        Instant now = Instant.ofEpochMilli(1_735_689_600_000L);
        Instant expectedCutoff = Instant.ofEpochMilli(1_735_689_600_000L - 14L * 24L * 60L * 60L * 1000L);
        when(sancaiAssetRepository.listExpiredDraftIds(expectedCutoff, 10))
                .thenReturn(List.of(SancaiEntryDraftIdCodec.toDomain(21L)));
        ClassicsCleanupApplicationServiceImpl service =
                new ClassicsCleanupApplicationServiceImpl(sancaiAssetRepository, mock(ClassicsContentRepository.class));

        List<CleanupTarget> targets = service.listTargets("expired_draft", now, 14, 10);

        assertEquals(1, targets.size());
        assertEquals("draft", targets.get(0).getTargetType());
        assertEquals(21L, targets.get(0).getTargetId());
    }

    @Test
    void unsupportedCleanupTypeShouldReturnEmptyTargets() {
        ClassicsCleanupApplicationServiceImpl service = new ClassicsCleanupApplicationServiceImpl(
                mock(SancaiAssetRepository.class), mock(ClassicsContentRepository.class));

        List<CleanupTarget> targets = service.listTargets("EXPIRED_BACKUP", Instant.now(), null, null);

        assertEquals(0, targets.size());
    }
}

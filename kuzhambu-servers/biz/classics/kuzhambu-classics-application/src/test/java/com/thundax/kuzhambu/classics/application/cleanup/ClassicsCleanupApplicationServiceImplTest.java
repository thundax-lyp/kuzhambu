package com.thundax.kuzhambu.classics.application.cleanup;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.classics.application.cleanup.service.ClassicsCleanupApplicationService.CleanupExecutionResult;
import com.thundax.kuzhambu.classics.application.cleanup.service.ClassicsCleanupApplicationService.CleanupTarget;
import com.thundax.kuzhambu.classics.application.cleanup.service.impl.ClassicsCleanupApplicationServiceImpl;
import com.thundax.kuzhambu.classics.domain.content.repository.ClassicsContentRepository;
import com.thundax.kuzhambu.classics.domain.sancai.repository.SancaiAssetRepository;
import com.thundax.kuzhambu.classics.domain.sharing.model.valueobject.ClassicsShareLinkId;
import com.thundax.kuzhambu.classics.domain.sharing.repository.ClassicsSharingRepository;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;

class ClassicsCleanupApplicationServiceImplTest {

    @Test
    void listTargetsShouldMapExpiredShareIds() {
        ClassicsSharingRepository sharingRepository = mock(ClassicsSharingRepository.class);
        Date now = new Date(1_735_689_600_000L);
        when(sharingRepository.listExpiredShareLinkIds(now, 50))
                .thenReturn(List.of(ClassicsShareLinkId.of(11L), ClassicsShareLinkId.of(12L)));
        ClassicsCleanupApplicationServiceImpl service = new ClassicsCleanupApplicationServiceImpl(
                sharingRepository, mock(SancaiAssetRepository.class), mock(ClassicsContentRepository.class));

        List<CleanupTarget> targets = service.listTargets("expired_share", now, null, 50);

        assertEquals(2, targets.size());
        assertEquals("share", targets.get(0).getTargetType());
        assertEquals(11L, targets.get(0).getTargetId());
    }

    @Test
    void executeTargetShouldReturnFailureWithoutThrowingWhenSingleItemFails() {
        ClassicsSharingRepository sharingRepository = mock(ClassicsSharingRepository.class);
        when(sharingRepository.markShareLinkExpired(ClassicsShareLinkId.of(11L)))
                .thenThrow(new IllegalStateException("db unavailable"));
        ClassicsCleanupApplicationServiceImpl service = new ClassicsCleanupApplicationServiceImpl(
                sharingRepository, mock(SancaiAssetRepository.class), mock(ClassicsContentRepository.class));

        CleanupExecutionResult result = service.executeTarget("EXPIRED_SHARE", 11L);

        assertEquals(false, result.isSuccess());
        assertEquals("share", result.getTargetType());
        assertEquals(11L, result.getTargetId());
        assertEquals("db unavailable", result.getFailureReason());
    }

    @Test
    void unsupportedCleanupTypeShouldReturnEmptyTargets() {
        ClassicsCleanupApplicationServiceImpl service = new ClassicsCleanupApplicationServiceImpl(
                mock(ClassicsSharingRepository.class),
                mock(SancaiAssetRepository.class),
                mock(ClassicsContentRepository.class));

        List<CleanupTarget> targets = service.listTargets("EXPIRED_BACKUP", new Date(), null, null);

        assertEquals(0, targets.size());
    }
}

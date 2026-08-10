package com.thundax.kuzhambu.classics.application.sancai;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.thundax.kuzhambu.classics.application.content.command.ContentVersionCommand;
import com.thundax.kuzhambu.classics.application.content.service.ClassicsContentApplicationService;
import com.thundax.kuzhambu.classics.application.publication.support.ClassicsPublicationWriteGuard;
import com.thundax.kuzhambu.classics.application.publication.support.ClassicsPublicationWriteOperation;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiEntryCommand;
import com.thundax.kuzhambu.classics.application.sancai.command.SancaiEntryStatusCommand;
import com.thundax.kuzhambu.classics.application.sancai.query.SancaiEntryQuery;
import com.thundax.kuzhambu.classics.application.sancai.service.impl.SancaiApplicationServiceImpl;
import com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentVersionIdCodec;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentChangeType;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentId;
import com.thundax.kuzhambu.classics.domain.sancai.codec.SancaiCategoryIdCodec;
import com.thundax.kuzhambu.classics.domain.sancai.codec.SancaiEntryIdCodec;
import com.thundax.kuzhambu.classics.domain.sancai.codec.SancaiVolumeIdCodec;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiCategoryOverview;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntry;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiVolume;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryImageStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryLifecycleStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryRefinementStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryTranslationStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryVisualAssetStatus;
import com.thundax.kuzhambu.classics.domain.sancai.repository.SancaiRepository;
import com.thundax.kuzhambu.common.core.exception.BizException;
import com.thundax.kuzhambu.common.core.page.PageQuery;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import com.thundax.kuzhambu.discovery.facade.DiscoverySearchPublicationFacade;
import com.thundax.kuzhambu.discovery.facade.request.DiscoverySearchPublicationCandidatePageFacadeRequest;
import com.thundax.kuzhambu.discovery.facade.request.DiscoverySearchPublicationCategoryAggregationFacadeRequest;
import com.thundax.kuzhambu.discovery.facade.request.DiscoverySearchPublicationReferenceFacadeRequest;
import com.thundax.kuzhambu.discovery.facade.response.DiscoverySearchPublicationCandidateFacadeResponse;
import com.thundax.kuzhambu.discovery.facade.response.DiscoverySearchPublicationCandidatePageFacadeResponse;
import com.thundax.kuzhambu.discovery.facade.response.DiscoverySearchPublicationCategoryAggregationFacadeResponse;
import com.thundax.kuzhambu.discovery.facade.response.DiscoverySearchPublicationProbeFacadeResponse;
import java.time.Instant;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class SancaiApplicationServiceImplTest {

    @Test
    void updateAndDeleteShouldUsePublicationWriteGuard() {
        SancaiRepository repository = mock(SancaiRepository.class);
        ClassicsContentApplicationService contentApplicationService = mock(ClassicsContentApplicationService.class);
        ClassicsPublicationWriteGuard writeGuard = mock(ClassicsPublicationWriteGuard.class);
        SancaiApplicationServiceImpl service = new SancaiApplicationServiceImpl(
                repository, contentApplicationService, writeGuard, mock(DiscoverySearchPublicationFacade.class));
        SancaiEntry entry = existingEntry(1001L, SancaiEntryLifecycleStatus.DRAFT);
        when(repository.getEntryById(SancaiEntryIdCodec.toDomain(1001L))).thenReturn(entry);
        when(repository.getVolumeById(SancaiVolumeIdCodec.toDomain(2001L))).thenReturn(volume(2001L));
        when(repository.updateEntry(any())).thenReturn(1);
        versionEntryOnEnsure(contentApplicationService, 1);

        service.updateEntry(privateDraftCommand(1001L));
        service.deleteEntry(SancaiEntryIdCodec.toDomain(1001L));

        verify(writeGuard)
                .requireWritable(
                        ClassicsContentType.SANCAI_ENTRY,
                        new ClassicsContentId(1001L),
                        ClassicsPublicationWriteOperation.EDIT);
        verify(writeGuard).prepareDeletion(ClassicsContentType.SANCAI_ENTRY, new ClassicsContentId(1001L));
    }

    @Test
    void addEntryShouldVersionEntry() {
        SancaiRepository repository = mock(SancaiRepository.class);
        ClassicsContentApplicationService contentApplicationService = mock(ClassicsContentApplicationService.class);
        SancaiApplicationServiceImpl service = service(repository, contentApplicationService);
        when(repository.getVolumeById(SancaiVolumeIdCodec.toDomain(2001L))).thenReturn(volume(2001L));
        when(repository.maxEntryPriority()).thenReturn(9);
        when(repository.insertEntry(any())).thenReturn(SancaiEntryIdCodec.toDomain(1001L));
        versionEntryOnEnsure(contentApplicationService, 3);

        service.addEntry(publicCommand(null));

        verify(contentApplicationService).ensureVersioned(any(ContentVersionCommand.class));
    }

    @Test
    void updateEntryShouldVersionEntry() {
        SancaiRepository repository = mock(SancaiRepository.class);
        ClassicsContentApplicationService contentApplicationService = mock(ClassicsContentApplicationService.class);
        SancaiApplicationServiceImpl service = service(repository, contentApplicationService);
        SancaiEntry entry = existingEntry(1002L, SancaiEntryLifecycleStatus.DRAFT);
        entry.setPriority(12);
        when(repository.getEntryById(SancaiEntryIdCodec.toDomain(1002L))).thenReturn(entry);
        when(repository.getVolumeById(SancaiVolumeIdCodec.toDomain(2001L))).thenReturn(volume(2001L));
        when(repository.updateEntry(any())).thenReturn(1);
        versionEntryOnEnsure(contentApplicationService, 4);

        service.updateEntry(privateDraftCommand(1002L));

        verify(contentApplicationService).ensureVersioned(any(ContentVersionCommand.class));
    }

    @Test
    void updateEntryShouldMoveAcrossVolumeAndAppendPriority() {
        SancaiRepository repository = mock(SancaiRepository.class);
        ClassicsContentApplicationService contentApplicationService = mock(ClassicsContentApplicationService.class);
        SancaiApplicationServiceImpl service = service(repository, contentApplicationService);
        SancaiEntry currentEntry = existingEntry(1010L, SancaiEntryLifecycleStatus.PUBLISHED);
        currentEntry.setPriority(12);
        when(repository.getEntryById(SancaiEntryIdCodec.toDomain(1010L))).thenReturn(currentEntry);
        when(repository.getVolumeById(SancaiVolumeIdCodec.toDomain(3002L))).thenReturn(volume(3002L));
        when(repository.maxEntryPriority()).thenReturn(99);
        when(repository.updateEntry(any())).thenReturn(1);
        versionEntryOnEnsure(contentApplicationService, 9);

        service.updateEntry(publicCommand(1010L, 3002L));

        ArgumentCaptor<SancaiEntry> entryCaptor = ArgumentCaptor.forClass(SancaiEntry.class);
        verify(repository, times(2)).updateEntry(entryCaptor.capture());
        SancaiEntry savedEntry = entryCaptor.getAllValues().get(0);
        assertEquals(SancaiVolumeIdCodec.toDomain(3002L), savedEntry.getVolumeId());
        assertEquals(100, savedEntry.getPriority());
        verify(contentApplicationService)
                .ensureVersioned(new ContentVersionCommand(savedEntry, ClassicsContentChangeType.MANUAL_SAVE, "手动保存"));
    }

    @Test
    void updateEntryShouldKeepPriorityWhenVolumeUnchanged() {
        SancaiRepository repository = mock(SancaiRepository.class);
        ClassicsContentApplicationService contentApplicationService = mock(ClassicsContentApplicationService.class);
        SancaiApplicationServiceImpl service = service(repository, contentApplicationService);
        SancaiEntry currentEntry = existingEntry(1011L, SancaiEntryLifecycleStatus.PUBLISHED);
        currentEntry.setPriority(44);
        when(repository.getEntryById(SancaiEntryIdCodec.toDomain(1011L))).thenReturn(currentEntry);
        when(repository.getVolumeById(SancaiVolumeIdCodec.toDomain(2001L))).thenReturn(volume(2001L));
        when(repository.updateEntry(any())).thenReturn(1);
        versionEntryOnEnsure(contentApplicationService, 10);

        service.updateEntry(publicCommand(1011L));

        ArgumentCaptor<SancaiEntry> entryCaptor = ArgumentCaptor.forClass(SancaiEntry.class);
        verify(repository, times(2)).updateEntry(entryCaptor.capture());
        assertEquals(44, entryCaptor.getAllValues().get(0).getPriority());
        verify(repository, never()).maxEntryPriority();
    }

    @Test
    void updateEntryShouldRejectMissingTargetVolume() {
        SancaiRepository repository = mock(SancaiRepository.class);
        ClassicsContentApplicationService contentApplicationService = mock(ClassicsContentApplicationService.class);
        SancaiApplicationServiceImpl service = service(repository, contentApplicationService);
        SancaiEntry currentEntry = existingEntry(1012L, SancaiEntryLifecycleStatus.PUBLISHED);
        when(repository.getEntryById(SancaiEntryIdCodec.toDomain(1012L))).thenReturn(currentEntry);
        when(repository.getVolumeById(SancaiVolumeIdCodec.toDomain(9090L))).thenReturn(null);

        assertThrows(BizException.class, () -> service.updateEntry(publicCommand(1012L, 9090L)));

        verify(repository, never()).updateEntry(any());
        verify(contentApplicationService, never()).ensureVersioned(any(ContentVersionCommand.class));
    }

    @Test
    void addEntryShouldRejectMissingTargetVolume() {
        SancaiRepository repository = mock(SancaiRepository.class);
        ClassicsContentApplicationService contentApplicationService = mock(ClassicsContentApplicationService.class);
        SancaiApplicationServiceImpl service = service(repository, contentApplicationService);
        when(repository.getVolumeById(SancaiVolumeIdCodec.toDomain(9091L))).thenReturn(null);

        assertThrows(BizException.class, () -> service.addEntry(publicCommand(null, 9091L)));

        verify(repository, never()).insertEntry(any());
        verify(contentApplicationService, never()).ensureVersioned(any(ContentVersionCommand.class));
    }

    @Test
    void changeEntryStatusShouldVersionWhenBecomingPublished() {
        SancaiRepository repository = mock(SancaiRepository.class);
        ClassicsContentApplicationService contentApplicationService = mock(ClassicsContentApplicationService.class);
        SancaiApplicationServiceImpl service = service(repository, contentApplicationService);
        SancaiEntry entry = existingEntry(1003L, SancaiEntryLifecycleStatus.DRAFT);
        when(repository.getEntryById(SancaiEntryIdCodec.toDomain(1003L))).thenReturn(entry);
        when(repository.updateEntry(any())).thenReturn(1);
        versionEntryOnEnsure(contentApplicationService, 5);
        SancaiEntryStatusCommand command = new SancaiEntryStatusCommand(1003L, SancaiEntryLifecycleStatus.PUBLISHED);

        service.changeEntryStatus(command);

        verify(contentApplicationService)
                .ensureVersioned(new ContentVersionCommand(entry, ClassicsContentChangeType.MANUAL_SAVE, "发布条目"));
    }

    @Test
    void changeEntryStatusShouldAllowExpectedLifecycleTransitionsAndVersionEntry() {
        assertLifecycleTransition(
                1101L, SancaiEntryLifecycleStatus.DRAFT, SancaiEntryLifecycleStatus.PUBLISHED, "发布条目");
        assertLifecycleTransition(
                1102L, SancaiEntryLifecycleStatus.PUBLISHED, SancaiEntryLifecycleStatus.OFFLINE, "下线条目");
        assertLifecycleTransition(
                1103L, SancaiEntryLifecycleStatus.OFFLINE, SancaiEntryLifecycleStatus.PUBLISHED, "恢复发布条目");
    }

    @Test
    void changeEntryStatusShouldRejectInvalidLifecycleTransitions() {
        assertInvalidLifecycleTransition(1201L, SancaiEntryLifecycleStatus.PUBLISHED, SancaiEntryLifecycleStatus.DRAFT);
        assertInvalidLifecycleTransition(1202L, SancaiEntryLifecycleStatus.OFFLINE, SancaiEntryLifecycleStatus.DRAFT);
    }

    @Test
    void changeEntryStatusShouldRejectWhenPermissionContextLacksSancaiEdit() {
        SancaiRepository repository = mock(SancaiRepository.class);
        ClassicsContentApplicationService contentApplicationService = mock(ClassicsContentApplicationService.class);
        SancaiApplicationServiceImpl service = service(repository, contentApplicationService);
        SancaiEntryStatusCommand command = new SancaiEntryStatusCommand(
                1203L, SancaiEntryLifecycleStatus.PUBLISHED, Set.of("classics:sancai:view"));

        assertThrows(BizException.class, () -> service.changeEntryStatus(command));

        verify(repository, never()).getEntryById(any());
        verify(repository, never()).updateEntry(any());
        verify(contentApplicationService, never()).ensureVersioned(any(ContentVersionCommand.class));
    }

    @Test
    void pageEntriesShouldReturnEmptyWhenPermissionContextLacksSancaiView() {
        SancaiRepository repository = mock(SancaiRepository.class);
        SancaiApplicationServiceImpl service = service(repository, null);
        SancaiEntryQuery query = new SancaiEntryQuery();
        query.setOperatorPermissions(Set.of("classics:content:view"));

        PageResult<SancaiEntry> result = service.pageEntries(query, new PageQuery(1, 20));

        assertEquals(0, result.getTotalCount());
        assertEquals(0, result.getRecords().size());
        verify(repository, never())
                .pageEntries(any(), any(), any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt());
    }

    @Test
    void pageEntriesShouldForwardCategoryFilterToRepository() {
        SancaiRepository repository = mock(SancaiRepository.class);
        SancaiApplicationServiceImpl service = service(repository, null);
        SancaiEntryQuery query = new SancaiEntryQuery();
        query.setCategoryId(2L);
        query.setVolumeId(101L);
        query.setKeyword("天文");

        service.pageEntries(query, new PageQuery(1, 20));

        verify(repository)
                .pageEntries(
                        eq(SancaiCategoryIdCodec.toDomain(2L)),
                        eq(SancaiVolumeIdCodec.toDomain(101L)),
                        eq("天文"),
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        any(),
                        eq(1),
                        eq(20));
    }

    @Test
    void listPortalReadyCategoryOverviewsShouldUseDiscoveryAggregationAndBoundedRepresentativeIds() {
        SancaiRepository repository = mock(SancaiRepository.class);
        DiscoverySearchPublicationFacade discoveryFacade = mock(DiscoverySearchPublicationFacade.class);
        SancaiApplicationServiceImpl service = new SancaiApplicationServiceImpl(
                repository, null, mock(ClassicsPublicationWriteGuard.class), discoveryFacade);
        when(discoveryFacade.listReadyCandidateCategoryAggregations(any()))
                .thenReturn(List.of(categoryAggregation("11", 13, "1001"), categoryAggregation("12", 3, "1002")));
        when(repository.listCategoryRepresentativeOverviewsByEntryIds(List.of(1001L, 1002L), SortDirection.ASC))
                .thenReturn(List.of(new SancaiCategoryOverview(
                        SancaiCategoryIdCodec.toDomain(11L), 0, 0, SancaiEntryIdCodec.toDomain(1001L), null, "天文图")));

        List<SancaiCategoryOverview> overviews = service.listPortalReadyCategoryOverviews();

        ArgumentCaptor<DiscoverySearchPublicationCategoryAggregationFacadeRequest> requestCaptor =
                ArgumentCaptor.forClass(DiscoverySearchPublicationCategoryAggregationFacadeRequest.class);
        verify(discoveryFacade).listReadyCandidateCategoryAggregations(requestCaptor.capture());
        assertEquals("SANCAI_ENTRY", requestCaptor.getValue().getContentType());
        verify(discoveryFacade, never()).pageReadyCandidates(any());
        verify(repository).listCategoryRepresentativeOverviewsByEntryIds(List.of(1001L, 1002L), SortDirection.ASC);
        assertEquals(2, overviews.size());
        assertEquals(13, overviews.get(0).getPublicEntryCount());
        assertEquals(SancaiEntryIdCodec.toDomain(1001L), overviews.get(0).getRepresentativeEntryId());
        assertEquals(3, overviews.get(1).getPublicEntryCount());
    }

    @Test
    void pagePortalReadyEntriesShouldHydrateDiscoveryCandidatesInReturnedOrder() {
        SancaiRepository repository = mock(SancaiRepository.class);
        DiscoverySearchPublicationFacade discoveryFacade = mock(DiscoverySearchPublicationFacade.class);
        SancaiApplicationServiceImpl service = new SancaiApplicationServiceImpl(
                repository, null, mock(ClassicsPublicationWriteGuard.class), discoveryFacade);
        when(discoveryFacade.pageReadyCandidates(any()))
                .thenReturn(DiscoverySearchPublicationCandidatePageFacadeResponse.builder()
                        .pageNo(1)
                        .pageSize(20)
                        .totalCount(2)
                        .records(List.of(candidate("1002", "11", "21"), candidate("1001", "11", "21")))
                        .build());
        when(repository.listEntriesByIds(
                        List.of(SancaiEntryIdCodec.toDomain(1002L), SancaiEntryIdCodec.toDomain(1001L))))
                .thenReturn(List.of(
                        existingEntry(1001L, SancaiEntryLifecycleStatus.DRAFT),
                        existingEntry(1002L, SancaiEntryLifecycleStatus.DRAFT)));
        SancaiEntryQuery query = new SancaiEntryQuery();
        query.setCategoryId(11L);
        query.setVolumeId(21L);
        query.setKeyword("天地");

        PageResult<SancaiEntry> result = service.pagePortalReadyEntries(query, new PageQuery(1, 20));

        ArgumentCaptor<DiscoverySearchPublicationCandidatePageFacadeRequest> requestCaptor =
                ArgumentCaptor.forClass(DiscoverySearchPublicationCandidatePageFacadeRequest.class);
        verify(discoveryFacade).pageReadyCandidates(requestCaptor.capture());
        assertEquals("SANCAI_ENTRY", requestCaptor.getValue().getContentType());
        assertEquals("11", requestCaptor.getValue().getCategoryId());
        assertEquals("21", requestCaptor.getValue().getVolumeId());
        assertEquals("天地", requestCaptor.getValue().getKeyword());
        assertEquals(2, result.getTotalCount());
        assertEquals(1002L, result.getRecords().get(0).getId().value());
        assertEquals(1001L, result.getRecords().get(1).getId().value());
        verify(repository, never())
                .pageEntries(any(), any(), any(), any(), any(), any(), any(), any(), any(), anyInt(), anyInt());
    }

    @Test
    void isPortalReadyEntryShouldOnlyAcceptReadyNonDeletedProbe() {
        SancaiRepository repository = mock(SancaiRepository.class);
        DiscoverySearchPublicationFacade discoveryFacade = mock(DiscoverySearchPublicationFacade.class);
        SancaiApplicationServiceImpl service = new SancaiApplicationServiceImpl(
                repository, null, mock(ClassicsPublicationWriteGuard.class), discoveryFacade);
        when(discoveryFacade.probe(any()))
                .thenReturn(DiscoverySearchPublicationProbeFacadeResponse.builder()
                        .present(true)
                        .publicationStatus("READY")
                        .deleted(false)
                        .build());

        boolean ready = service.isPortalReadyEntry(SancaiEntryIdCodec.toDomain(1001L));

        ArgumentCaptor<DiscoverySearchPublicationReferenceFacadeRequest> requestCaptor =
                ArgumentCaptor.forClass(DiscoverySearchPublicationReferenceFacadeRequest.class);
        verify(discoveryFacade).probe(requestCaptor.capture());
        assertEquals("SANCAI_ENTRY:1001", requestCaptor.getValue().getDocumentId());
        assertEquals(true, ready);
    }

    void deleteEntryShouldVersionAndDeleteEntry() {
        SancaiRepository repository = mock(SancaiRepository.class);
        ClassicsContentApplicationService contentApplicationService = mock(ClassicsContentApplicationService.class);
        SancaiApplicationServiceImpl service = service(repository, contentApplicationService);
        SancaiEntry entry = existingEntry(1005L, SancaiEntryLifecycleStatus.PUBLISHED);
        when(repository.getEntryById(SancaiEntryIdCodec.toDomain(1005L))).thenReturn(entry);
        versionEntryOnEnsure(contentApplicationService, 7);

        service.deleteEntry(SancaiEntryIdCodec.toDomain(1005L));

        verify(contentApplicationService)
                .ensureVersioned(new ContentVersionCommand(entry, ClassicsContentChangeType.MANUAL_SAVE, "手动删除"));
        verify(repository).deleteEntryById(SancaiEntryIdCodec.toDomain(1005L));
    }

    private static void versionEntryOnEnsure(
            ClassicsContentApplicationService contentApplicationService, int versionNo) {
        doAnswer(invocation -> {
                    ContentVersionCommand command = invocation.getArgument(0);
                    SancaiEntry entry = (SancaiEntry) command.content();
                    entry.setCurrentVersionId(ClassicsContentVersionIdCodec.toDomain((long) versionNo));
                    entry.setCurrentVersionNo(versionNo);
                    entry.setCurrentVersionedAt(Instant.ofEpochMilli(2_000L + versionNo));
                    return null;
                })
                .when(contentApplicationService)
                .ensureVersioned(any(ContentVersionCommand.class));
    }

    private static void assertLifecycleTransition(
            long id,
            SancaiEntryLifecycleStatus currentStatus,
            SancaiEntryLifecycleStatus targetStatus,
            String expectedSummary) {
        SancaiRepository repository = mock(SancaiRepository.class);
        ClassicsContentApplicationService contentApplicationService = mock(ClassicsContentApplicationService.class);
        SancaiApplicationServiceImpl service = service(repository, contentApplicationService);
        SancaiEntry entry = existingEntry(id, currentStatus);
        when(repository.getEntryById(SancaiEntryIdCodec.toDomain(id))).thenReturn(entry);
        when(repository.updateEntry(any())).thenReturn(1);
        versionEntryOnEnsure(contentApplicationService, 20);

        service.changeEntryStatus(new SancaiEntryStatusCommand(id, targetStatus, Set.of("classics:sancai:edit")));

        ArgumentCaptor<SancaiEntry> entryCaptor = ArgumentCaptor.forClass(SancaiEntry.class);
        verify(repository).updateEntry(entryCaptor.capture());
        SancaiEntry updatedEntry = entryCaptor.getValue();
        assertEquals(targetStatus, updatedEntry.getLifecycleStatus());
        assertNotNull(updatedEntry.getContentUpdatedAt());
        assertEquals(ClassicsContentVersionIdCodec.toDomain(20L), updatedEntry.getCurrentVersionId());
        assertEquals(20, updatedEntry.getCurrentVersionNo());
        assertNotNull(updatedEntry.getCurrentVersionedAt());
        verify(contentApplicationService)
                .ensureVersioned(new ContentVersionCommand(
                        updatedEntry, ClassicsContentChangeType.MANUAL_SAVE, expectedSummary));
    }

    private static void assertInvalidLifecycleTransition(
            long id, SancaiEntryLifecycleStatus currentStatus, SancaiEntryLifecycleStatus targetStatus) {
        SancaiRepository repository = mock(SancaiRepository.class);
        ClassicsContentApplicationService contentApplicationService = mock(ClassicsContentApplicationService.class);
        SancaiApplicationServiceImpl service = service(repository, contentApplicationService);
        SancaiEntry entry = existingEntry(id, currentStatus);
        when(repository.getEntryById(SancaiEntryIdCodec.toDomain(id))).thenReturn(entry);

        assertThrows(
                BizException.class,
                () -> service.changeEntryStatus(
                        new SancaiEntryStatusCommand(id, targetStatus, Set.of("classics:sancai:edit"))));

        assertEquals(currentStatus, entry.getLifecycleStatus());
        verify(repository, never()).updateEntry(any());
        verify(contentApplicationService, never()).ensureVersioned(any(ContentVersionCommand.class));
    }

    private static SancaiEntry existingEntry(long id, SancaiEntryLifecycleStatus lifecycleStatus) {
        SancaiEntry entry = new SancaiEntry();
        entry.setId(SancaiEntryIdCodec.toDomain(id));
        entry.setVolumeId(SancaiVolumeIdCodec.toDomain(2001L));
        entry.setTitle("条目");
        entry.setLifecycleStatus(lifecycleStatus);
        entry.setTranslationStatus(SancaiEntryTranslationStatus.MISSING);
        entry.setImageStatus(SancaiEntryImageStatus.MISSING);
        entry.setVisualAssetStatus(SancaiEntryVisualAssetStatus.MISSING);
        entry.setRefinementStatus(SancaiEntryRefinementStatus.RAW);
        return entry;
    }

    private static DiscoverySearchPublicationCandidateFacadeResponse candidate(
            String contentId, String categoryId, String volumeId) {
        return DiscoverySearchPublicationCandidateFacadeResponse.builder()
                .contentType("SANCAI_ENTRY")
                .contentId(contentId)
                .categoryId(categoryId)
                .volumeId(volumeId)
                .build();
    }

    private static DiscoverySearchPublicationCategoryAggregationFacadeResponse categoryAggregation(
            String categoryId, long readyEntryCount, String representativeContentId) {
        return DiscoverySearchPublicationCategoryAggregationFacadeResponse.builder()
                .categoryId(categoryId)
                .readyEntryCount(readyEntryCount)
                .representativeContentId(representativeContentId)
                .build();
    }

    private static SancaiApplicationServiceImpl service(
            SancaiRepository repository, ClassicsContentApplicationService contentApplicationService) {
        return new SancaiApplicationServiceImpl(
                repository,
                contentApplicationService,
                mock(ClassicsPublicationWriteGuard.class),
                mock(DiscoverySearchPublicationFacade.class));
    }

    private static SancaiVolume volume(long id) {
        SancaiVolume volume = new SancaiVolume();
        volume.setId(SancaiVolumeIdCodec.toDomain(id));
        return volume;
    }

    private static SancaiEntryCommand publicCommand(Long id) {
        return publicCommand(id, 2001L);
    }

    private static SancaiEntryCommand publicCommand(Long id, Long volumeId) {
        return new SancaiEntryCommand(
                id,
                volumeId,
                "条目",
                "原文",
                "译文",
                "摘要",
                SancaiEntryLifecycleStatus.PUBLISHED,
                SancaiEntryTranslationStatus.MISSING,
                SancaiEntryImageStatus.MISSING,
                SancaiEntryVisualAssetStatus.MISSING,
                SancaiEntryRefinementStatus.RAW);
    }

    private static SancaiEntryCommand privateDraftCommand(Long id) {
        return new SancaiEntryCommand(
                id,
                2001L,
                "条目",
                "原文",
                "译文",
                "摘要",
                SancaiEntryLifecycleStatus.DRAFT,
                SancaiEntryTranslationStatus.MISSING,
                SancaiEntryImageStatus.MISSING,
                SancaiEntryVisualAssetStatus.MISSING,
                SancaiEntryRefinementStatus.RAW);
    }
}

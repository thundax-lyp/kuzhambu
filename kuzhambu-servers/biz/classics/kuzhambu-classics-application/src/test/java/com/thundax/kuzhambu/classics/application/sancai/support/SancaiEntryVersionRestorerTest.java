package com.thundax.kuzhambu.classics.application.sancai.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentIdCodec;
import com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentVersionIdCodec;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentQaPair;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentTag;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentVersion;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.content.repository.ClassicsContentRepository;
import com.thundax.kuzhambu.classics.domain.publication.codec.ClassicsPublicationJobIdCodec;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationTransitionStatus;
import com.thundax.kuzhambu.classics.domain.sancai.codec.SancaiEntryIdCodec;
import com.thundax.kuzhambu.classics.domain.sancai.codec.SancaiVolumeIdCodec;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntry;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryLifecycleStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryVisibility;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiEntryId;
import com.thundax.kuzhambu.common.core.exception.BizException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class SancaiEntryVersionRestorerTest {
    private ClassicsContentRepository contentRepository;

    @BeforeEach
    void setUp() {
        contentRepository = mock(ClassicsContentRepository.class);
        when(contentRepository.listTags(any(), any(), any())).thenReturn(java.util.List.of());
        when(contentRepository.listQaPairs(any(), any(), any())).thenReturn(java.util.List.of());
    }

    @Test
    void restoreSnapshotShouldRestoreContentAndMoveEntryToGlobalTail() {
        FakeRepository repository = new FakeRepository();
        SancaiEntryVersionRestorer restorer = restorer(repository);

        SancaiEntry restored = restorer.restoreSnapshot(version(100L, snapshotJson(100L)));

        assertEquals(SancaiEntryIdCodec.toDomain(100L), restored.getId());
        assertEquals(SancaiVolumeIdCodec.toDomain(10L), restored.getVolumeId());
        assertEquals("历史标题", restored.getTitle());
        assertEquals("历史原文", restored.getOriginalText());
        assertEquals("历史译文", restored.getTranslationText());
        assertEquals("历史摘要", restored.getSummary());
        assertEquals(SancaiEntryLifecycleStatus.OFFLINE, restored.getLifecycleStatus());
        assertEquals(ClassicsPublicationTransitionStatus.NONE, restored.getTransitionStatus());
        assertEquals(ClassicsPublicationJobIdCodec.toDomain(900L), restored.getCurrentPublicationJobId());
        assertEquals(SancaiEntryVisibility.PUBLIC, restored.getVisibility());
        assertEquals(8, restored.getPriority());
        assertNotNull(restored.getContentUpdatedAt());
        assertEquals(restored, repository.restoredEntry);
    }

    @Test
    void markVersionedShouldPersistVersionMarker() {
        FakeRepository repository = new FakeRepository();
        SancaiEntryVersionRestorer restorer = restorer(repository);
        SancaiEntry entry = new SancaiEntry();
        entry.setId(SancaiEntryIdCodec.toDomain(100L));
        entry.setCurrentVersionId(ClassicsContentVersionIdCodec.toDomain(2L));
        entry.setCurrentVersionNo(2);

        restorer.markVersioned(entry);

        assertEquals(ClassicsContentVersionIdCodec.toDomain(2L), repository.restoredEntry.getCurrentVersionId());
        assertEquals(2, repository.restoredEntry.getCurrentVersionNo());
    }

    @Test
    void restoreSnapshotShouldRejectWrongContentType() {
        FakeRepository repository = new FakeRepository();
        SancaiEntryVersionRestorer restorer = restorer(repository);
        ClassicsContentVersion version = version(100L, snapshotJson(100L));
        version.setContentType(ClassicsContentType.WANGQI_DOCUMENT);

        assertThrows(BizException.class, () -> restorer.restoreSnapshot(version));
    }

    @Test
    void restoreSnapshotShouldRejectMismatchedSnapshotOwner() {
        FakeRepository repository = new FakeRepository();
        SancaiEntryVersionRestorer restorer = restorer(repository);

        assertThrows(BizException.class, () -> restorer.restoreSnapshot(version(100L, snapshotJson(101L))));
    }

    @Test
    void restoreSnapshotShouldRejectMissingCurrentEntry() {
        FakeRepository repository = new FakeRepository();
        repository.currentEntry = null;
        SancaiEntryVersionRestorer restorer = restorer(repository);

        assertThrows(BizException.class, () -> restorer.restoreSnapshot(version(100L, snapshotJson(100L))));
    }

    @Test
    void restoreSnapshotShouldReplaceTagsAndQaPairs() {
        FakeRepository repository = new FakeRepository();
        SancaiEntryVersionRestorer restorer = restorer(repository);

        restorer.restoreSnapshot(version(100L, snapshotJson(100L)));

        ArgumentCaptor<ClassicsContentTag> tagCaptor = ArgumentCaptor.forClass(ClassicsContentTag.class);
        verify(contentRepository).insertTag(tagCaptor.capture());
        assertEquals("历史标签", tagCaptor.getValue().getTagNameSnapshot());
        assertEquals(1, tagCaptor.getValue().getPriority());

        ArgumentCaptor<ClassicsContentQaPair> qaCaptor = ArgumentCaptor.forClass(ClassicsContentQaPair.class);
        verify(contentRepository).insertQaPair(qaCaptor.capture());
        assertEquals("历史问题", qaCaptor.getValue().getQuestion());
        assertEquals("历史答案", qaCaptor.getValue().getAnswer());
        assertEquals(1, qaCaptor.getValue().getPriority());
    }

    private SancaiEntryVersionRestorer restorer(FakeRepository repository) {
        return new SancaiEntryVersionRestorer(repository, contentRepository, new ObjectMapper(), null);
    }

    private static ClassicsContentVersion version(Long contentId, String snapshotJson) {
        ClassicsContentVersion version = new ClassicsContentVersion();
        version.setId(ClassicsContentVersionIdCodec.toDomain(1L));
        version.setContentType(ClassicsContentType.SANCAI_ENTRY);
        version.setContentId(ClassicsContentIdCodec.toDomain(contentId));
        version.setVersionNo(1);
        version.setSnapshotJson(snapshotJson);
        return version;
    }

    private static String snapshotJson(Long contentId) {
        return """
                {
                  "contentType": "SANCAI_ENTRY",
                  "contentId": %d,
                  "contentUpdatedAt": "2026-06-20T10:00:00Z",
                  "volumeId": 10,
                  "title": "历史标题",
                  "originalText": "历史原文",
                  "translationText": "历史译文",
                  "summary": "历史摘要",
                  "lifecycleStatus": "PUBLISHED",
                  "visibility": "PUBLIC",
                  "translationStatus": "READY",
                  "imageStatus": "READY",
                  "visualAssetStatus": "READY",
                  "refinementStatus": "COMPLETE",
                  "priority": 1,
                  "images": [],
                  "tags": [
                    {
                      "id": 1,
                      "tagId": null,
                      "tagNameSnapshot": "历史标签",
                      "source": "MANUAL",
                      "status": "ACTIVE",
                      "priority": 3
                    }
                  ],
                  "qaPairs": [
                    {
                      "id": 2,
                      "question": "历史问题",
                      "answer": "历史答案",
                      "source": "MANUAL",
                      "priority": 4
                    }
                  ]
                }
                """
                .formatted(contentId);
    }

    private static final class FakeRepository extends FakeSancaiRepositorySupport {
        private SancaiEntry currentEntry = currentEntry();
        private SancaiEntry restoredEntry;

        @Override
        public SancaiEntry getEntryById(SancaiEntryId id) {
            return currentEntry;
        }

        @Override
        public int maxEntryPriority() {
            return 7;
        }

        @Override
        public int updateRestoredEntry(SancaiEntry entry) {
            restoredEntry = entry;
            return 1;
        }

        private static SancaiEntry currentEntry() {
            SancaiEntry entry = new SancaiEntry();
            entry.setId(SancaiEntryIdCodec.toDomain(100L));
            entry.setLifecycleStatus(SancaiEntryLifecycleStatus.OFFLINE);
            entry.setTransitionStatus(ClassicsPublicationTransitionStatus.NONE);
            entry.setCurrentPublicationJobId(ClassicsPublicationJobIdCodec.toDomain(900L));
            entry.setVisibility(SancaiEntryVisibility.PUBLIC);
            return entry;
        }
    }
}

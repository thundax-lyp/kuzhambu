package com.thundax.kuzhambu.classics.application.sancai.support;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentVersion;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentId;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentVersionId;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntry;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryLifecycleStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryVisibility;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiEntryId;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiVolumeId;
import com.thundax.kuzhambu.common.core.exception.BizException;
import org.junit.jupiter.api.Test;

class SancaiEntryVersionRestorerTest {

    @Test
    void restoreSnapshotShouldRestoreContentAndMoveEntryToGlobalTail() {
        FakeRepository repository = new FakeRepository();
        SancaiEntryVersionRestorer restorer = new SancaiEntryVersionRestorer(repository, new ObjectMapper());

        SancaiEntry restored = restorer.restoreSnapshot(version(100L, snapshotJson(100L)));

        assertEquals(SancaiEntryId.of(100L), restored.getId());
        assertEquals(SancaiVolumeId.of(10L), restored.getVolumeId());
        assertEquals("历史标题", restored.getTitle());
        assertEquals("历史原文", restored.getOriginalText());
        assertEquals("历史译文", restored.getTranslationText());
        assertEquals("历史摘要", restored.getSummary());
        assertEquals(SancaiEntryLifecycleStatus.PUBLISHED, restored.getLifecycleStatus());
        assertEquals(SancaiEntryVisibility.PUBLIC, restored.getVisibility());
        assertEquals(8, restored.getPriority());
        assertNotNull(restored.getContentUpdatedAt());
        assertEquals(restored, repository.restoredEntry);
    }

    @Test
    void markVersionedShouldPersistVersionMarker() {
        FakeRepository repository = new FakeRepository();
        SancaiEntryVersionRestorer restorer = new SancaiEntryVersionRestorer(repository, new ObjectMapper());
        SancaiEntry entry = new SancaiEntry();
        entry.setId(SancaiEntryId.of(100L));
        entry.setCurrentVersionId(ClassicsContentVersionId.of(2L));
        entry.setCurrentVersionNo(2);

        restorer.markVersioned(entry);

        assertEquals(ClassicsContentVersionId.of(2L), repository.restoredEntry.getCurrentVersionId());
        assertEquals(2, repository.restoredEntry.getCurrentVersionNo());
    }

    @Test
    void restoreSnapshotShouldRejectWrongContentType() {
        FakeRepository repository = new FakeRepository();
        SancaiEntryVersionRestorer restorer = new SancaiEntryVersionRestorer(repository, new ObjectMapper());
        ClassicsContentVersion version = version(100L, snapshotJson(100L));
        version.setContentType(ClassicsContentType.WANGQI_DOCUMENT);

        assertThrows(BizException.class, () -> restorer.restoreSnapshot(version));
    }

    @Test
    void restoreSnapshotShouldRejectMismatchedSnapshotOwner() {
        FakeRepository repository = new FakeRepository();
        SancaiEntryVersionRestorer restorer = new SancaiEntryVersionRestorer(repository, new ObjectMapper());

        assertThrows(BizException.class, () -> restorer.restoreSnapshot(version(100L, snapshotJson(101L))));
    }

    @Test
    void restoreSnapshotShouldRejectMissingCurrentEntry() {
        FakeRepository repository = new FakeRepository();
        repository.currentEntry = null;
        SancaiEntryVersionRestorer restorer = new SancaiEntryVersionRestorer(repository, new ObjectMapper());

        assertThrows(BizException.class, () -> restorer.restoreSnapshot(version(100L, snapshotJson(100L))));
    }

    private static ClassicsContentVersion version(Long contentId, String snapshotJson) {
        ClassicsContentVersion version = new ClassicsContentVersion();
        version.setId(ClassicsContentVersionId.of(1L));
        version.setContentType(ClassicsContentType.SANCAI_ENTRY);
        version.setContentId(ClassicsContentId.of(contentId));
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
                  "priority": 1
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
            entry.setId(SancaiEntryId.of(100L));
            return entry;
        }
    }
}

package com.thundax.kuzhambu.classics.application.content;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.thundax.kuzhambu.classics.application.content.service.impl.ClassicsContentApplicationServiceImpl;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentExportJob;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentQaPair;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentTag;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentVersion;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentChangeType;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentExportJobId;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentId;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentQaPairId;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentTagId;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentVersionId;
import com.thundax.kuzhambu.classics.domain.content.repository.ClassicsContentRepository;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntry;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiEntryId;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.junit.jupiter.api.Test;

class ClassicsContentApplicationServiceImplTest {

    @Test
    void ensureVersionedShouldInsertVersionAndBackfillContentMarker() {
        FakeRepository repository = new FakeRepository();
        ClassicsContentApplicationServiceImpl service = new ClassicsContentApplicationServiceImpl(repository, null);
        SancaiEntry entry = new SancaiEntry();
        entry.setId(SancaiEntryId.of(100L));
        entry.setTitle("entry");
        entry.setContentUpdatedAt(new Date(1_000L));

        ClassicsContentVersion version = service.ensureVersioned(entry, ClassicsContentChangeType.MANUAL_SAVE, "手动保存");

        assertNotNull(version.getId());
        assertEquals(1, version.getVersionNo());
        assertEquals(1, repository.insertedVersions.size());
        assertEquals(version.getId(), entry.getCurrentVersionId());
        assertEquals(version.getVersionNo(), entry.getCurrentVersionNo());
        assertNotNull(version.getSnapshotJson());
    }

    @Test
    void ensureVersionedShouldNotInsertWhenContentIsAlreadyVersioned() {
        FakeRepository repository = new FakeRepository();
        ClassicsContentVersion existing = existingVersion(9L, 2, new Date(2_000L));
        repository.insertedVersions.add(existing);
        ClassicsContentApplicationServiceImpl service = new ClassicsContentApplicationServiceImpl(repository, null);
        SancaiEntry entry = new SancaiEntry();
        entry.setId(SancaiEntryId.of(100L));
        entry.setCurrentVersionId(existing.getId());
        entry.setCurrentVersionNo(existing.getVersionNo());
        entry.setCurrentVersionedAt(existing.getVersionedAt());
        entry.setContentUpdatedAt(new Date(1_000L));

        ClassicsContentVersion version = service.ensureVersioned(entry, ClassicsContentChangeType.MANUAL_SAVE, "手动保存");

        assertEquals(existing, version);
        assertEquals(1, repository.insertedVersions.size());
    }

    private static ClassicsContentVersion existingVersion(Long id, int versionNo, Date versionedAt) {
        ClassicsContentVersion version = new ClassicsContentVersion();
        version.setId(ClassicsContentVersionId.of(id));
        version.setVersionNo(versionNo);
        version.setVersionedAt(versionedAt);
        return version;
    }

    private static final class FakeRepository implements ClassicsContentRepository {
        private final List<ClassicsContentVersion> insertedVersions = new ArrayList<>();

        @Override
        public List<ClassicsContentVersion> listVersions(String contentType, ClassicsContentId contentId) {
            return insertedVersions;
        }

        @Override
        public ClassicsContentVersionId insertVersion(ClassicsContentVersion version) {
            ClassicsContentVersionId id = ClassicsContentVersionId.of((long) insertedVersions.size() + 1L);
            version.setId(id);
            insertedVersions.add(version);
            return id;
        }

        @Override
        public List<ClassicsContentTag> listTags(
                String contentType, ClassicsContentId contentId, SortDirection sortDirection) {
            return List.of();
        }

        @Override
        public List<ClassicsContentTag> listTags(SortDirection sortDirection) {
            return List.of();
        }

        @Override
        public int maxTagPriority() {
            return 0;
        }

        @Override
        public ClassicsContentTagId insertTag(ClassicsContentTag tag) {
            return null;
        }

        @Override
        public ClassicsContentTag getTagById(ClassicsContentTagId id) {
            return null;
        }

        @Override
        public int updateTagPriority(ClassicsContentTag tag) {
            return 0;
        }

        @Override
        public int updateTag(ClassicsContentTag tag) {
            return 0;
        }

        @Override
        public int deleteTagById(ClassicsContentTagId id) {
            return 0;
        }

        @Override
        public List<ClassicsContentQaPair> listQaPairs(
                String contentType, ClassicsContentId contentId, SortDirection sortDirection) {
            return List.of();
        }

        @Override
        public List<ClassicsContentQaPair> listQaPairs(SortDirection sortDirection) {
            return List.of();
        }

        @Override
        public int maxQaPairPriority() {
            return 0;
        }

        @Override
        public ClassicsContentQaPairId insertQaPair(ClassicsContentQaPair qaPair) {
            return null;
        }

        @Override
        public ClassicsContentQaPair getQaPairById(ClassicsContentQaPairId id) {
            return null;
        }

        @Override
        public int updateQaPairPriority(ClassicsContentQaPair qaPair) {
            return 0;
        }

        @Override
        public int updateQaPair(ClassicsContentQaPair qaPair) {
            return 0;
        }

        @Override
        public int deleteQaPairById(ClassicsContentQaPairId id) {
            return 0;
        }

        @Override
        public ClassicsContentVersion getVersionById(ClassicsContentVersionId id) {
            return null;
        }

        @Override
        public ClassicsContentExportJobId insertExportJob(ClassicsContentExportJob exportJob) {
            return null;
        }

        @Override
        public int updateExportJob(ClassicsContentExportJob exportJob) {
            return 0;
        }

        @Override
        public Page<ClassicsContentExportJob> pageExportJobs(
                String contentType, String exportKind, String status, int pageNo, int pageSize) {
            return new Page<>();
        }
    }
}

package com.thundax.kuzhambu.classics.domain.content.repository;

import com.thundax.kuzhambu.classics.domain.common.model.valueobject.StorageObjectId;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentExportJob;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentQaPair;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentTag;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentVersion;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentExportJobId;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentId;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentQaPairId;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentTagId;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentVersionId;
import com.thundax.kuzhambu.classics.domain.mingcustoms.model.entity.MingCustomsEntry;
import com.thundax.kuzhambu.classics.domain.publication.model.entity.ClassicsPublicationContent;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntry;
import com.thundax.kuzhambu.classics.domain.wangqi.model.entity.WangqiDocument;
import com.thundax.kuzhambu.common.core.page.PageResult;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import java.time.Instant;
import java.util.List;

public interface ClassicsContentRepository {

    List<ClassicsContentTag> listTags(String contentType, ClassicsContentId contentId, SortDirection sortDirection);

    List<ClassicsContentTag> listTags(SortDirection sortDirection);

    int maxTagPriority(String contentType, ClassicsContentId contentId);

    ClassicsContentTagId insertTag(ClassicsContentTag tag);

    ClassicsContentTag getByTagId(ClassicsContentTagId id);

    int updateTagPriority(ClassicsContentTag tag);

    int updateTag(ClassicsContentTag tag);

    int deleteByTagId(String contentType, ClassicsContentId contentId, ClassicsContentTagId id);

    List<ClassicsContentQaPair> listQaPairs(
            String contentType, ClassicsContentId contentId, SortDirection sortDirection);

    List<ClassicsContentQaPair> listQaPairs(SortDirection sortDirection);

    int maxQaPairPriority();

    ClassicsContentQaPairId insertQaPair(ClassicsContentQaPair qaPair);

    ClassicsContentQaPair getByQaPairId(ClassicsContentQaPairId id);

    int updateQaPairPriority(ClassicsContentQaPair qaPair);

    int updateQaPair(ClassicsContentQaPair qaPair);

    int deleteByQaPairId(ClassicsContentQaPairId id);

    List<ClassicsContentVersion> listVersions(String contentType, ClassicsContentId contentId);

    default ClassicsContentVersion getByLatestVersion(ClassicsContentType contentType, ClassicsContentId contentId) {
        List<ClassicsContentVersion> versions = listVersions(contentType.value(), contentId);
        return versions.isEmpty() ? null : versions.get(0);
    }

    default int getByLatestVersionNo(ClassicsContentType contentType, ClassicsContentId contentId) {
        ClassicsContentVersion version = getByLatestVersion(contentType, contentId);
        return version == null ? 0 : version.getVersionNo();
    }

    default void updateContentForVersionLock(ClassicsContentType contentType, ClassicsContentId contentId) {}

    ClassicsPublicationContent getByPublicationContentForLock(
            ClassicsContentType contentType, ClassicsContentId contentId);

    int updatePublicationContentState(ClassicsPublicationContent expectedState, ClassicsPublicationContent targetState);

    ClassicsContentVersionId insertVersion(ClassicsContentVersion version);

    ClassicsContentVersion getByVersionId(ClassicsContentVersionId id);

    int deleteByVersions(String contentType, ClassicsContentId contentId);

    SancaiEntry getBySancaiEntryForAiApply(ClassicsContentId contentId);

    int updateSancaiEntryAiFields(SancaiEntry entry);

    default int updateSancaiEntryVersionMarkers(SancaiEntry entry) {
        return 0;
    }

    WangqiDocument getByWangqiDocumentForAiApply(ClassicsContentId contentId);

    int updateWangqiDocumentAiFields(WangqiDocument document);

    default int updateWangqiDocumentVersionMarkers(WangqiDocument document) {
        return 0;
    }

    MingCustomsEntry getByMingCustomsEntryForAiApply(ClassicsContentId contentId);

    int updateMingCustomsEntryAiFields(MingCustomsEntry entry);

    default int updateMingCustomsEntryVersionMarkers(MingCustomsEntry entry) {
        return 0;
    }

    int deleteByAiTags(String contentType, ClassicsContentId contentId);

    int deleteByAiQaPairs(String contentType, ClassicsContentId contentId);

    ClassicsContentExportJobId insertExportJob(ClassicsContentExportJob exportJob);

    ClassicsContentExportJob getByExportJobId(ClassicsContentExportJobId id);

    int updateExportJob(ClassicsContentExportJob exportJob);

    int updateExportJobCompleted(
            ClassicsContentExportJobId id,
            StorageObjectId storageObjectId,
            Instant expiresAt,
            int itemCount,
            int assetCount);

    int updateExportJobFailed(ClassicsContentExportJobId id);

    int updateExportJobExpired(ClassicsContentExportJobId id);

    int deleteByExportJobId(ClassicsContentExportJobId id);

    default List<ClassicsContentExportJobId> listExpiredExportJobIds(Instant now, int limit) {
        return List.of();
    }

    PageResult<ClassicsContentExportJob> page(
            String contentType, String exportKind, String status, int pageNo, int pageSize);
}

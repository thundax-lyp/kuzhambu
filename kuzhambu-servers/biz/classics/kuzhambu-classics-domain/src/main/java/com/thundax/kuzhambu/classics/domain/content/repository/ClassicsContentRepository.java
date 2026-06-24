package com.thundax.kuzhambu.classics.domain.content.repository;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
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
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiEntry;
import com.thundax.kuzhambu.classics.domain.wangqi.model.entity.WangqiDocument;
import com.thundax.kuzhambu.common.core.sort.SortDirection;
import java.util.Date;
import java.util.List;

public interface ClassicsContentRepository {

    List<ClassicsContentTag> listTags(String contentType, ClassicsContentId contentId, SortDirection sortDirection);

    int maxTagPriority(String contentType, ClassicsContentId contentId);

    ClassicsContentTagId insertTag(ClassicsContentTag tag);

    ClassicsContentTag getTagById(ClassicsContentTagId id);

    int updateTagPriority(ClassicsContentTag tag);

    int updateTag(ClassicsContentTag tag);

    int deleteTagById(String contentType, ClassicsContentId contentId, ClassicsContentTagId id);

    List<ClassicsContentQaPair> listQaPairs(
            String contentType, ClassicsContentId contentId, SortDirection sortDirection);

    List<ClassicsContentQaPair> listQaPairs(SortDirection sortDirection);

    int maxQaPairPriority();

    ClassicsContentQaPairId insertQaPair(ClassicsContentQaPair qaPair);

    ClassicsContentQaPair getQaPairById(ClassicsContentQaPairId id);

    int updateQaPairPriority(ClassicsContentQaPair qaPair);

    int updateQaPair(ClassicsContentQaPair qaPair);

    int deleteQaPairById(ClassicsContentQaPairId id);

    List<ClassicsContentVersion> listVersions(String contentType, ClassicsContentId contentId);

    default ClassicsContentVersion latestVersion(ClassicsContentType contentType, ClassicsContentId contentId) {
        List<ClassicsContentVersion> versions = listVersions(contentType.value(), contentId);
        return versions.isEmpty() ? null : versions.get(0);
    }

    default int latestVersionNo(ClassicsContentType contentType, ClassicsContentId contentId) {
        ClassicsContentVersion version = latestVersion(contentType, contentId);
        return version == null ? 0 : version.getVersionNo();
    }

    ClassicsContentVersionId insertVersion(ClassicsContentVersion version);

    ClassicsContentVersion getVersionById(ClassicsContentVersionId id);

    int deleteVersions(String contentType, ClassicsContentId contentId);

    SancaiEntry getSancaiEntryForAiApply(ClassicsContentId contentId);

    int updateSancaiEntryAiFields(SancaiEntry entry);

    default int updateSancaiEntryVersionMarkers(SancaiEntry entry) {
        return 0;
    }

    WangqiDocument getWangqiDocumentForAiApply(ClassicsContentId contentId);

    int updateWangqiDocumentAiFields(WangqiDocument document);

    default int updateWangqiDocumentVersionMarkers(WangqiDocument document) {
        return 0;
    }

    MingCustomsEntry getMingCustomsEntryForAiApply(ClassicsContentId contentId);

    int updateMingCustomsEntryAiFields(MingCustomsEntry entry);

    default int updateMingCustomsEntryVersionMarkers(MingCustomsEntry entry) {
        return 0;
    }

    int deleteAiTags(String contentType, ClassicsContentId contentId);

    int deleteAiQaPairs(String contentType, ClassicsContentId contentId);

    ClassicsContentExportJobId insertExportJob(ClassicsContentExportJob exportJob);

    ClassicsContentExportJob getExportJobById(ClassicsContentExportJobId id);

    int updateExportJob(ClassicsContentExportJob exportJob);

    int markExportJobCompleted(
            ClassicsContentExportJobId id,
            StorageObjectId storageObjectId,
            Date expiresAt,
            int itemCount,
            int assetCount);

    int markExportJobFailed(ClassicsContentExportJobId id);

    int markExportJobExpired(ClassicsContentExportJobId id);

    Page<ClassicsContentExportJob> pageExportJobs(
            String contentType, String exportKind, String status, int pageNo, int pageSize);
}

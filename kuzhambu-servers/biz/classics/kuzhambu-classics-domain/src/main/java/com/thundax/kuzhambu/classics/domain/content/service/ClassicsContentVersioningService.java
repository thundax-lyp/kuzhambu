package com.thundax.kuzhambu.classics.domain.content.service;

import com.thundax.kuzhambu.classics.domain.content.model.Versionable;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentVersion;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentChangeType;
import java.util.Date;

public class ClassicsContentVersioningService {

    public boolean needsVersion(Versionable content) {
        if (content == null) {
            return false;
        }
        if (content.currentVersionId() == null || content.currentVersionedAt() == null) {
            return true;
        }
        Date contentUpdatedAt = content.contentUpdatedAt();
        return contentUpdatedAt != null && contentUpdatedAt.after(content.currentVersionedAt());
    }

    public int nextVersionNo(int latestVersionNo) {
        return latestVersionNo + 1;
    }

    public ClassicsContentVersion newVersion(
            Versionable content,
            int versionNo,
            Date versionedAt,
            String snapshotJson,
            ClassicsContentChangeType changeType,
            String changeSummary) {
        ClassicsContentVersion version = new ClassicsContentVersion();
        version.setContentType(content.contentType());
        version.setContentId(content.contentId());
        version.setVersionNo(versionNo);
        version.setVersionedAt(versionedAt);
        version.setSnapshotJson(snapshotJson);
        version.setChangeType(changeType);
        version.setChangeSummary(changeSummary);
        return version;
    }

    public void markVersioned(Versionable content, ClassicsContentVersion version) {
        if (content != null && version != null) {
            content.markVersioned(version);
        }
    }
}

package com.thundax.kuzhambu.classics.domain.content.support;

import com.thundax.kuzhambu.classics.domain.content.model.Versionable;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentVersion;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentChangeType;
import java.time.Instant;

public class ClassicsContentVersioningSupport {

    public boolean needsVersion(Versionable content) {
        if (content == null) {
            return false;
        }
        if (content.currentVersionId() == null || content.currentVersionedAt() == null) {
            return true;
        }
        Instant contentUpdatedAt = content.contentUpdatedAt();
        return contentUpdatedAt != null && contentUpdatedAt.isAfter(content.currentVersionedAt());
    }

    public int nextVersionNo(int latestVersionNo) {
        return latestVersionNo + 1;
    }

    public ClassicsContentVersion newVersion(
            Versionable content,
            int versionNo,
            Instant versionedAt,
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

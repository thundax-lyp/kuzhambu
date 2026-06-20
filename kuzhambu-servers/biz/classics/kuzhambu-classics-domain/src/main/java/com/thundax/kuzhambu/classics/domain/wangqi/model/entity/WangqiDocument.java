package com.thundax.kuzhambu.classics.domain.wangqi.model.entity;

import com.thundax.kuzhambu.classics.domain.common.model.valueobject.StorageObjectId;
import com.thundax.kuzhambu.classics.domain.content.model.Versionable;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentVersion;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentId;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentVersionId;
import com.thundax.kuzhambu.classics.domain.wangqi.model.enums.WangqiContentFormat;
import com.thundax.kuzhambu.classics.domain.wangqi.model.enums.WangqiDocumentVisibility;
import com.thundax.kuzhambu.classics.domain.wangqi.model.valueobject.WangqiDocumentId;
import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WangqiDocument implements Versionable {
    private WangqiDocumentId id;
    private String title;
    private String summary;
    private WangqiContentFormat contentFormat;
    private String content;
    private Date documentTime;
    private StorageObjectId storageObjectId;
    private WangqiDocumentVisibility visibility;
    private ClassicsContentVersionId currentVersionId;
    private Integer currentVersionNo;
    private Date currentVersionedAt;
    private Date contentUpdatedAt;

    public WangqiDocument(
            WangqiDocumentId id,
            String title,
            String summary,
            WangqiContentFormat contentFormat,
            String content,
            Date documentTime,
            StorageObjectId storageObjectId,
            WangqiDocumentVisibility visibility) {
        this.id = id;
        this.title = title;
        this.summary = summary;
        this.contentFormat = contentFormat;
        this.content = content;
        this.documentTime = documentTime;
        this.storageObjectId = storageObjectId;
        this.visibility = visibility;
    }

    @Override
    public ClassicsContentType contentType() {
        return ClassicsContentType.WANGQI_DOCUMENT;
    }

    @Override
    public ClassicsContentId contentId() {
        return ClassicsContentId.ofNullable(id == null ? null : id.value());
    }

    @Override
    public ClassicsContentVersionId currentVersionId() {
        return currentVersionId;
    }

    @Override
    public Integer currentVersionNo() {
        return currentVersionNo;
    }

    @Override
    public Date currentVersionedAt() {
        return currentVersionedAt;
    }

    @Override
    public Date contentUpdatedAt() {
        return contentUpdatedAt;
    }

    @Override
    public void markVersioned(ClassicsContentVersion version) {
        this.currentVersionId = version.getId();
        this.currentVersionNo = version.getVersionNo();
        this.currentVersionedAt = version.getVersionedAt();
    }
}

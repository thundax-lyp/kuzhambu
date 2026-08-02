package com.thundax.kuzhambu.classics.domain.wangqi.model.entity;

import com.thundax.kuzhambu.classics.domain.common.model.valueobject.StorageObjectId;
import com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentIdCodec;
import com.thundax.kuzhambu.classics.domain.content.model.Versionable;
import com.thundax.kuzhambu.classics.domain.content.model.entity.ClassicsContentVersion;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentId;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentVersionId;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationLifecycleStatus;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationTransitionStatus;
import com.thundax.kuzhambu.classics.domain.publication.model.valueobject.ClassicsPublicationJobId;
import com.thundax.kuzhambu.classics.domain.wangqi.model.enums.WangqiContentFormat;
import com.thundax.kuzhambu.classics.domain.wangqi.model.valueobject.WangqiDocumentId;
import java.time.Instant;
import java.util.List;
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
    private Instant documentTime;
    private StorageObjectId storageObjectId;
    private ClassicsPublicationLifecycleStatus lifecycleStatus;
    private ClassicsPublicationTransitionStatus transitionStatus;
    private ClassicsPublicationJobId currentPublicationJobId;
    private ClassicsContentVersionId currentVersionId;
    private Integer currentVersionNo;
    private Instant currentVersionedAt;
    private Instant contentUpdatedAt;
    private List<WangqiDocumentEvent> events;

    public WangqiDocument(
            WangqiDocumentId id,
            String title,
            String summary,
            WangqiContentFormat contentFormat,
            String content,
            Instant documentTime,
            StorageObjectId storageObjectId) {
        this.id = id;
        this.title = title;
        this.summary = summary;
        this.contentFormat = contentFormat;
        this.content = content;
        this.documentTime = documentTime;
        this.storageObjectId = storageObjectId;
    }

    @Override
    public ClassicsContentType contentType() {
        return ClassicsContentType.WANGQI_DOCUMENT;
    }

    @Override
    public ClassicsContentId contentId() {
        return ClassicsContentIdCodec.toDomain(id == null ? null : id.value());
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
    public Instant currentVersionedAt() {
        return currentVersionedAt;
    }

    @Override
    public Instant contentUpdatedAt() {
        return contentUpdatedAt;
    }

    @Override
    public void markVersioned(ClassicsContentVersion version) {
        this.currentVersionId = version.getId();
        this.currentVersionNo = version.getVersionNo();
        this.currentVersionedAt = version.getVersionedAt();
    }
}

package com.thundax.kuzhambu.classics.infra.wangqi.persistence.assembler;

import com.thundax.kuzhambu.classics.domain.common.codec.StorageObjectIdCodec;
import com.thundax.kuzhambu.classics.domain.content.codec.ClassicsContentVersionIdCodec;
import com.thundax.kuzhambu.classics.domain.publication.codec.ClassicsPublicationJobIdCodec;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationLifecycleStatus;
import com.thundax.kuzhambu.classics.domain.publication.model.enums.ClassicsPublicationTransitionStatus;
import com.thundax.kuzhambu.classics.domain.wangqi.codec.WangqiDocumentIdCodec;
import com.thundax.kuzhambu.classics.domain.wangqi.model.entity.WangqiDocument;
import com.thundax.kuzhambu.classics.domain.wangqi.model.enums.WangqiContentFormat;
import com.thundax.kuzhambu.classics.infra.wangqi.persistence.dataobject.WangqiDocumentDO;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

public final class WangqiDocumentPersistenceAssembler {

    private WangqiDocumentPersistenceAssembler() {}

    public static WangqiDocumentDO toObject(WangqiDocument entity) {
        if (entity == null) {
            return null;
        }
        WangqiDocumentDO dataObject = new WangqiDocumentDO();
        dataObject.setId(WangqiDocumentIdCodec.toValue(entity.getId()));
        dataObject.setTitle(entity.getTitle());
        dataObject.setSummary(entity.getSummary());
        dataObject.setContentFormat(
                entity.getContentFormat() == null
                        ? null
                        : entity.getContentFormat().value());
        dataObject.setContent(entity.getContent());
        dataObject.setDocumentTime(entity.getDocumentTime());
        dataObject.setStorageObjectId(StorageObjectIdCodec.toValue(entity.getStorageObjectId()));
        dataObject.setLifecycleStatus(value(entity.getLifecycleStatus()));
        dataObject.setTransitionStatus(value(entity.getTransitionStatus()));
        dataObject.setCurrentPublicationJobId(
                ClassicsPublicationJobIdCodec.toValue(entity.getCurrentPublicationJobId()));
        dataObject.setCurrentVersionId(ClassicsContentVersionIdCodec.toValue(entity.getCurrentVersionId()));
        dataObject.setCurrentVersionNo(entity.getCurrentVersionNo());
        dataObject.setCurrentVersionedAt(entity.getCurrentVersionedAt());
        dataObject.setContentUpdatedAt(contentUpdatedAt(entity.getContentUpdatedAt()));
        return dataObject;
    }

    public static WangqiDocument toDomain(WangqiDocumentDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        WangqiDocument document = new WangqiDocument(
                WangqiDocumentIdCodec.toDomain(dataObject.getId()),
                dataObject.getTitle(),
                dataObject.getSummary(),
                dataObject.getContentFormat() == null ? null : WangqiContentFormat.from(dataObject.getContentFormat()),
                dataObject.getContent(),
                dataObject.getDocumentTime(),
                StorageObjectIdCodec.toDomain(dataObject.getStorageObjectId()));
        document.setLifecycleStatus(parseLifecycle(dataObject.getLifecycleStatus()));
        document.setTransitionStatus(parseTransition(dataObject.getTransitionStatus()));
        document.setCurrentPublicationJobId(
                ClassicsPublicationJobIdCodec.toDomain(dataObject.getCurrentPublicationJobId()));
        document.setCurrentVersionId(ClassicsContentVersionIdCodec.toDomain(dataObject.getCurrentVersionId()));
        document.setCurrentVersionNo(dataObject.getCurrentVersionNo());
        document.setCurrentVersionedAt(dataObject.getCurrentVersionedAt());
        document.setContentUpdatedAt(dataObject.getContentUpdatedAt());
        return document;
    }

    public static List<WangqiDocument> toDomainList(List<WangqiDocumentDO> dataObjects) {
        List<WangqiDocument> entities = new ArrayList<>();
        if (dataObjects != null) {
            for (WangqiDocumentDO dataObject : dataObjects) {
                entities.add(toDomain(dataObject));
            }
        }
        return entities;
    }

    private static Instant contentUpdatedAt(Instant contentUpdatedAt) {
        return contentUpdatedAt == null ? Instant.now() : contentUpdatedAt;
    }

    private static String value(Enum<?> value) {
        return value == null ? null : value.name();
    }

    private static ClassicsPublicationLifecycleStatus parseLifecycle(String value) {
        return value == null ? null : ClassicsPublicationLifecycleStatus.valueOf(value);
    }

    private static ClassicsPublicationTransitionStatus parseTransition(String value) {
        return value == null ? null : ClassicsPublicationTransitionStatus.valueOf(value);
    }
}

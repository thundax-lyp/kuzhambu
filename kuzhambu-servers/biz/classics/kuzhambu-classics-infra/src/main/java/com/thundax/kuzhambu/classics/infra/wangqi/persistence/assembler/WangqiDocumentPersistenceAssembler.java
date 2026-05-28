package com.thundax.kuzhambu.classics.infra.wangqi.persistence.assembler;

import com.thundax.kuzhambu.classics.domain.common.codec.StorageObjectIdCodec;
import com.thundax.kuzhambu.classics.domain.wangqi.codec.WangqiDocumentIdCodec;
import com.thundax.kuzhambu.classics.domain.wangqi.model.entity.WangqiDocument;
import com.thundax.kuzhambu.classics.domain.wangqi.model.enums.WangqiContentFormat;
import com.thundax.kuzhambu.classics.domain.wangqi.model.enums.WangqiDocumentVisibility;
import com.thundax.kuzhambu.classics.infra.wangqi.persistence.dataobject.WangqiDocumentDO;
import java.util.ArrayList;
import java.util.List;

public final class WangqiDocumentPersistenceAssembler {

    private WangqiDocumentPersistenceAssembler() {}

    public static WangqiDocumentDO toObject(WangqiDocument entity) {
        if (entity == null) {
            return null;
        }
        return new WangqiDocumentDO(
                WangqiDocumentIdCodec.toValue(entity.getId()),
                entity.getTitle(),
                entity.getSummary(),
                entity.getContentFormat() == null
                        ? null
                        : entity.getContentFormat().value(),
                entity.getContent(),
                entity.getDocumentTime(),
                StorageObjectIdCodec.toValue(entity.getStorageObjectId()),
                entity.getVisibility() == null ? null : entity.getVisibility().value());
    }

    public static WangqiDocument toDomain(WangqiDocumentDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return new WangqiDocument(
                WangqiDocumentIdCodec.toDomain(dataObject.getId()),
                dataObject.getTitle(),
                dataObject.getSummary(),
                dataObject.getContentFormat() == null ? null : WangqiContentFormat.from(dataObject.getContentFormat()),
                dataObject.getContent(),
                dataObject.getDocumentTime(),
                StorageObjectIdCodec.toDomain(dataObject.getStorageObjectId()),
                dataObject.getVisibility() == null ? null : WangqiDocumentVisibility.from(dataObject.getVisibility()));
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
}

package com.thundax.kuzhambu.classics.infra.wangqi.persistence.assembler;

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
                entity.getId(),
                entity.getTitle(),
                entity.getSummary(),
                entity.getContentFormat() == null
                        ? null
                        : entity.getContentFormat().value(),
                entity.getContent(),
                entity.getDocumentTime(),
                entity.getStorageObjectId(),
                entity.getVisibility() == null ? null : entity.getVisibility().value());
    }

    public static WangqiDocument toDomain(WangqiDocumentDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return new WangqiDocument(
                dataObject.getId(),
                dataObject.getTitle(),
                dataObject.getSummary(),
                dataObject.getContentFormat() == null ? null : WangqiContentFormat.from(dataObject.getContentFormat()),
                dataObject.getContent(),
                dataObject.getDocumentTime(),
                dataObject.getStorageObjectId(),
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

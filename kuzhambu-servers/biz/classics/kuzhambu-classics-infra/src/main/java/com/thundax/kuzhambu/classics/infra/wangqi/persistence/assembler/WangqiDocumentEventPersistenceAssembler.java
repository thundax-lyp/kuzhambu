package com.thundax.kuzhambu.classics.infra.wangqi.persistence.assembler;

import com.thundax.kuzhambu.classics.domain.wangqi.codec.WangqiDocumentEventIdCodec;
import com.thundax.kuzhambu.classics.domain.wangqi.codec.WangqiDocumentIdCodec;
import com.thundax.kuzhambu.classics.domain.wangqi.model.entity.WangqiDocumentEvent;
import com.thundax.kuzhambu.classics.infra.wangqi.persistence.dataobject.WangqiDocumentEventDO;
import java.util.ArrayList;
import java.util.List;

public final class WangqiDocumentEventPersistenceAssembler {

    private WangqiDocumentEventPersistenceAssembler() {}

    public static WangqiDocumentEvent toDomain(WangqiDocumentEventDO dataObject) {
        if (dataObject == null) {
            return null;
        }
        return new WangqiDocumentEvent(
                WangqiDocumentEventIdCodec.toDomain(dataObject.getId()),
                WangqiDocumentIdCodec.toDomain(dataObject.getDocumentId()),
                dataObject.getTitle(),
                dataObject.getOccurredAt(),
                dataObject.getOccurredLabel(),
                dataObject.getSummary(),
                dataObject.getPriority());
    }

    public static List<WangqiDocumentEvent> toDomainList(List<WangqiDocumentEventDO> dataObjects) {
        List<WangqiDocumentEvent> events = new ArrayList<>();
        if (dataObjects != null) {
            for (WangqiDocumentEventDO dataObject : dataObjects) {
                events.add(toDomain(dataObject));
            }
        }
        return events;
    }
}

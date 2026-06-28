package com.thundax.kuzhambu.classics.facade.dto;

import java.util.Date;
import lombok.Builder;
import lombok.Getter;

@Getter
public class ClassicsSearchIndexSyncMessageFacadeDto {

    private final String eventId;
    private final ClassicsSearchIndexSyncEventFacadeDto eventType;
    private final String contentType;
    private final String contentId;
    private final Integer currentVersionNo;
    private final Date occurredAt;

    @Builder
    private ClassicsSearchIndexSyncMessageFacadeDto(
            String eventId,
            ClassicsSearchIndexSyncEventFacadeDto eventType,
            String contentType,
            String contentId,
            Integer currentVersionNo,
            Date occurredAt) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.contentType = contentType;
        this.contentId = contentId;
        this.currentVersionNo = currentVersionNo;
        this.occurredAt = occurredAt;
    }
}

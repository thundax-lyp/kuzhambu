package com.thundax.kuzhambu.classics.facade.dto;

import java.time.Instant;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class ClassicsSearchIndexSyncMessageFacadeDto {

    private final String eventId;
    private final ClassicsSearchIndexSyncEventFacadeDto eventType;
    private final String contentType;
    private final String contentId;
    private final Integer currentVersionNo;
    private final Instant occurredAt;
}

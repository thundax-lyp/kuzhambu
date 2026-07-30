package com.thundax.kuzhambu.classics.application.searchsync.model;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ClassicsSearchIndexSyncMessage {
    private String eventId;
    private ClassicsSearchIndexSyncEventType eventType;
    private String contentType;
    private String contentId;
    private Integer currentVersionNo;
    private Instant occurredAt;
}

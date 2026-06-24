package com.thundax.kuzhambu.classics.application.searchsync.model;

import java.util.Date;
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
    private Date occurredAt;
}

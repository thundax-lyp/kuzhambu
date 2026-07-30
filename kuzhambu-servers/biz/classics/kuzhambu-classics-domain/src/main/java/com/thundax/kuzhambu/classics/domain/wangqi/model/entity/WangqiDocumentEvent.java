package com.thundax.kuzhambu.classics.domain.wangqi.model.entity;

import com.thundax.kuzhambu.classics.domain.wangqi.model.valueobject.WangqiDocumentEventId;
import com.thundax.kuzhambu.classics.domain.wangqi.model.valueobject.WangqiDocumentId;
import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class WangqiDocumentEvent {
    private WangqiDocumentEventId id;
    private WangqiDocumentId documentId;
    private String title;
    private Instant occurredAt;
    private String occurredLabel;
    private String summary;
    private Integer priority;
}

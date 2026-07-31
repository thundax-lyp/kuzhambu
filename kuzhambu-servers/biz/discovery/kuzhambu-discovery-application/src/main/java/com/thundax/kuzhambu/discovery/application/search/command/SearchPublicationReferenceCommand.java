package com.thundax.kuzhambu.discovery.application.search.command;

import java.time.Instant;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SearchPublicationReferenceCommand {

    private final String documentId;
    private final String contentVersionId;
    private final Integer contentVersionNo;
    private final Instant occurredAt;
}

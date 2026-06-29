package com.thundax.kuzhambu.operations.application.cleanup.query;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OperationsCleanupPageQuery {
    private String cleanupType;
    private String cleanupStatus;
    private Long requesterUserId;
}

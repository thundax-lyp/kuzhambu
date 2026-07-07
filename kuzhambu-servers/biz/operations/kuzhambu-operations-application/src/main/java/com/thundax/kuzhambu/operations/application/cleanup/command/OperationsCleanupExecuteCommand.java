package com.thundax.kuzhambu.operations.application.cleanup.command;

import java.util.Date;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OperationsCleanupExecuteCommand {
    private String cleanupType;
    private Long requesterUserId;
    private Date requestedAt;
    private Integer retentionDays;
    private Integer limit;

    public OperationsCleanupExecuteCommand(String cleanupType, Long requesterUserId) {
        this.cleanupType = cleanupType;
        this.requesterUserId = requesterUserId;
    }
}

package com.thundax.kuzhambu.operations.application.cleanup.command;

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
}

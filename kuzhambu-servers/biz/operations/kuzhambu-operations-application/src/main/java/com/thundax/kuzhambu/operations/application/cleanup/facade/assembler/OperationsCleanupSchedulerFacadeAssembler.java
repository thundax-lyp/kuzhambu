package com.thundax.kuzhambu.operations.application.cleanup.facade.assembler;

import com.thundax.kuzhambu.operations.application.cleanup.command.OperationsCleanupExecuteCommand;
import com.thundax.kuzhambu.operations.application.cleanup.support.OperationsCleanupPolicies;
import java.time.Instant;
import java.util.Objects;
import org.springframework.lang.NonNull;

public final class OperationsCleanupSchedulerFacadeAssembler {

    private OperationsCleanupSchedulerFacadeAssembler() {}

    @NonNull
    public static OperationsCleanupExecuteCommand toCommand(
            @NonNull OperationsCleanupPolicies.CleanupPolicy policy, @NonNull Instant requestedAt) {
        Objects.requireNonNull(policy, "policy must not be null");
        Objects.requireNonNull(requestedAt, "requestedAt must not be null");
        return new OperationsCleanupExecuteCommand(
                policy.cleanupType(), null, requestedAt, policy.retentionDays(), policy.limit());
    }
}

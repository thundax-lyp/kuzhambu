package com.thundax.kuzhambu.operations.application.cleanup.query;

import com.thundax.kuzhambu.operations.domain.cleanup.model.valueobject.CleanupJobId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OperationsCleanupDetailQuery {
    private CleanupJobId cleanupId;
}

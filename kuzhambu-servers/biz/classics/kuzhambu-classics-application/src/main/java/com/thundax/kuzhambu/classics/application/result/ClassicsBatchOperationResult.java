package com.thundax.kuzhambu.classics.application.result;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class ClassicsBatchOperationResult {
    private final int successCount;
    private final int failureCount;
    private final List<ClassicsBatchOperationItemResult> successes;
    private final List<ClassicsBatchOperationItemResult> failures;

    public static ClassicsBatchOperationResult of(
            List<ClassicsBatchOperationItemResult> successes, List<ClassicsBatchOperationItemResult> failures) {
        List<ClassicsBatchOperationItemResult> safeSuccesses = successes == null ? List.of() : List.copyOf(successes);
        List<ClassicsBatchOperationItemResult> safeFailures = failures == null ? List.of() : List.copyOf(failures);
        return new ClassicsBatchOperationResult(safeSuccesses.size(), safeFailures.size(), safeSuccesses, safeFailures);
    }

    public static ClassicsBatchOperationResult empty() {
        return of(List.of(), List.of());
    }
}

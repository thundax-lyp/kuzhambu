package com.thundax.kuzhambu.operations.application.restore.support;

import com.thundax.kuzhambu.operations.domain.restore.model.valueobject.RestoreId;
import java.util.Date;
import java.util.concurrent.atomic.AtomicReference;
import org.springframework.stereotype.Component;

@Component
public class OperationsRestoreWriteBlocker {

    private final AtomicReference<RestoreId> activeRestoreId = new AtomicReference<>();

    public Date enable(RestoreId restoreId) {
        if (restoreId == null) {
            throw new IllegalArgumentException("Operations restore write block restoreId must not be null.");
        }
        if (!activeRestoreId.compareAndSet(null, restoreId)) {
            throw new IllegalStateException("Operations restore write block is already enabled.");
        }
        return new Date();
    }

    public Date disable(RestoreId restoreId) {
        RestoreId active = activeRestoreId.get();
        if (active == null) {
            return new Date();
        }
        if (!active.equals(restoreId)) {
            throw new IllegalStateException("Operations restore write block restoreId mismatch.");
        }
        activeRestoreId.compareAndSet(active, null);
        return new Date();
    }

    public boolean isEnabled() {
        return activeRestoreId.get() != null;
    }
}

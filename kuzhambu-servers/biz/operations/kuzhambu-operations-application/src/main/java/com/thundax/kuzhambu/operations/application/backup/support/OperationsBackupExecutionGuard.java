package com.thundax.kuzhambu.operations.application.backup.support;

import java.util.concurrent.atomic.AtomicBoolean;
import org.springframework.stereotype.Component;

@Component
public class OperationsBackupExecutionGuard {

    private final AtomicBoolean running = new AtomicBoolean(false);

    public boolean tryEnterBackup() {
        return running.compareAndSet(false, true);
    }

    public boolean tryEnterRestore() {
        return running.compareAndSet(false, true);
    }

    public void exit() {
        running.set(false);
    }
}

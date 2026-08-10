package com.thundax.kuzhambu.classics.application.sancai.command;

import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiEntryLifecycleStatus;
import java.util.Set;

public record SancaiEntryStatusCommand(
        Long id, SancaiEntryLifecycleStatus lifecycleStatus, Set<String> operatorPermissions) {
    public SancaiEntryStatusCommand(Long id, SancaiEntryLifecycleStatus lifecycleStatus) {
        this(id, lifecycleStatus, null);
    }
}

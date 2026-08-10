package com.thundax.kuzhambu.classics.application.sancai.assembler;

import com.thundax.kuzhambu.classics.application.sancai.command.SancaiShowcaseCommand;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiShowcase;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiShowcaseStatus;
import java.time.Instant;

public final class SancaiApplicationAssembler {

    private SancaiApplicationAssembler() {}

    public static SancaiShowcase toShowcase(SancaiShowcaseCommand command) {
        SancaiShowcase showcase = new SancaiShowcase();
        if (command == null) {
            return showcase;
        }
        showcase.setRequestedAt(command.requestedAt() == null ? Instant.now() : command.requestedAt());
        showcase.setStatus(command.status() == null ? SancaiShowcaseStatus.REQUESTED : command.status());
        showcase.setScopeJson(command.scopeJson());
        showcase.setScopeTitle(command.scopeTitle());
        showcase.setEntryCount(command.entryCount());
        showcase.setVisibilityRiskStatus(command.visibilityRiskStatus());
        return showcase;
    }
}

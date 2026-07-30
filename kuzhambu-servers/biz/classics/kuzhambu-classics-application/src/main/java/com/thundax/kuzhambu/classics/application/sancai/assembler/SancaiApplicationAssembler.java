package com.thundax.kuzhambu.classics.application.sancai.assembler;

import com.thundax.kuzhambu.classics.application.sancai.command.SancaiShowcaseCommand;
import com.thundax.kuzhambu.classics.domain.sancai.model.entity.SancaiShowcase;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiShowcaseStatus;
import java.util.Date;

public final class SancaiApplicationAssembler {

    private SancaiApplicationAssembler() {}

    public static SancaiShowcase toShowcase(SancaiShowcaseCommand command) {
        SancaiShowcase showcase = new SancaiShowcase();
        if (command == null) {
            return showcase;
        }
        showcase.setRequestedAt(command.getRequestedAt() == null ? new Date() : command.getRequestedAt());
        showcase.setStatus(command.getStatus() == null ? SancaiShowcaseStatus.REQUESTED : command.getStatus());
        showcase.setScopeJson(command.getScopeJson());
        showcase.setScopeTitle(command.getScopeTitle());
        showcase.setEntryCount(command.getEntryCount());
        showcase.setVisibilityRiskStatus(command.getVisibilityRiskStatus());
        return showcase;
    }
}

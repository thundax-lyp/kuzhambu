package com.thundax.kuzhambu.classics.application.sancai.command;

import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiEntryId;
import java.util.List;

public record SancaiEntrySortCommand(List<SancaiEntryId> orderedIds) {}

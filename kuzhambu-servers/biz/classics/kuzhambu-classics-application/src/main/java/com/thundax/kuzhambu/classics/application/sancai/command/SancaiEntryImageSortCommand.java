package com.thundax.kuzhambu.classics.application.sancai.command;

import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiEntryImageId;
import java.util.List;

public record SancaiEntryImageSortCommand(List<SancaiEntryImageId> orderedIds) {}

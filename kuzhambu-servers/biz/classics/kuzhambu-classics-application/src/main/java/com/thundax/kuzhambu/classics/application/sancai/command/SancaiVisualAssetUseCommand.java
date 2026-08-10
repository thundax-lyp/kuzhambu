package com.thundax.kuzhambu.classics.application.sancai.command;

import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiEntryId;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiVisualAssetId;

public record SancaiVisualAssetUseCommand(SancaiEntryId entryId, SancaiVisualAssetId visualAssetId) {}

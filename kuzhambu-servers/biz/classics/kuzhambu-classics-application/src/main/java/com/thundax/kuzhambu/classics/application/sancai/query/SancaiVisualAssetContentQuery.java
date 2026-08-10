package com.thundax.kuzhambu.classics.application.sancai.query;

import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiEntryId;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiVisualAssetId;

public record SancaiVisualAssetContentQuery(SancaiEntryId entryId, SancaiVisualAssetId visualAssetId) {}

package com.thundax.kuzhambu.classics.application.sancai.query;

import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiEntryId;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiEntryImageId;

public record SancaiImageContentQuery(SancaiEntryId entryId, SancaiEntryImageId imageId) {}

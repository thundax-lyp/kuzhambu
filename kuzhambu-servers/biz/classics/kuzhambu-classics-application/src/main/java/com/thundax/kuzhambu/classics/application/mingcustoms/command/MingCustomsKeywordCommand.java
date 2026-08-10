package com.thundax.kuzhambu.classics.application.mingcustoms.command;

import com.thundax.kuzhambu.classics.domain.mingcustoms.model.valueobject.MingCustomsEntryId;

public record MingCustomsKeywordCommand(MingCustomsEntryId customId, String keyword) {}

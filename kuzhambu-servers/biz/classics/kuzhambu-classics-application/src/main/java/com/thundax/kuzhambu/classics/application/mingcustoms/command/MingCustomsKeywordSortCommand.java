package com.thundax.kuzhambu.classics.application.mingcustoms.command;

import com.thundax.kuzhambu.classics.domain.mingcustoms.model.valueobject.MingCustomsKeywordId;
import java.util.List;

public record MingCustomsKeywordSortCommand(List<MingCustomsKeywordId> orderedIds) {}

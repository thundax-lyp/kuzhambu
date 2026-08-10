package com.thundax.kuzhambu.system.application.core.command;

import com.thundax.kuzhambu.system.domain.core.model.valueobject.DictId;
import java.util.List;

public record DictSortCommand(List<DictId> orderedIds) {}

package com.thundax.kuzhambu.classics.application.sancai.command;

import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiCategoryId;
import java.util.List;

public record SancaiCategorySortCommand(List<SancaiCategoryId> orderedIds) {}

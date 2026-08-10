package com.thundax.kuzhambu.classics.application.content.command;

import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentTagId;
import java.util.List;

public record ContentTagSortCommand(List<ClassicsContentTagId> orderedIds) {}

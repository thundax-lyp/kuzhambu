package com.thundax.kuzhambu.classics.application.content.command;

import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentQaPairId;
import java.util.List;

public record ContentQaPairSortCommand(List<ClassicsContentQaPairId> orderedIds) {}

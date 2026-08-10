package com.thundax.kuzhambu.classics.application.content.command;

import com.thundax.kuzhambu.classics.domain.content.model.Versionable;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentChangeType;

public record ContentVersionCommand(Versionable content, ClassicsContentChangeType changeType, String changeSummary) {}

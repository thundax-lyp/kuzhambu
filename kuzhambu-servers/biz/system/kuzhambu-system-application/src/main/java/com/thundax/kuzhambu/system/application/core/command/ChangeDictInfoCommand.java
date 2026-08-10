package com.thundax.kuzhambu.system.application.core.command;

import com.thundax.kuzhambu.system.domain.core.model.valueobject.DictId;

public record ChangeDictInfoCommand(DictId id, String type, String label, String value, String remarks) {}

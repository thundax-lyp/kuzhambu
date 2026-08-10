package com.thundax.kuzhambu.system.application.core.command;

import com.thundax.kuzhambu.system.application.core.query.LogQuery;

public record DeleteLogCommand(LogQuery query) {}

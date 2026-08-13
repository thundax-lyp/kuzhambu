package com.thundax.kuzhambu.knowledge.application.graph.command;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;

public record GraphMaterialImportCommand(
        ContentRef materialRef, String graphJson, String strategy, long materialLockVersion) {}

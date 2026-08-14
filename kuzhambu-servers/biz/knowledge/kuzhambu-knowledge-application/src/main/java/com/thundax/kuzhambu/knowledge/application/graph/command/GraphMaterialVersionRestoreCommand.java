package com.thundax.kuzhambu.knowledge.application.graph.command;

import com.thundax.kuzhambu.common.core.content.valueobject.ContentRef;

public record GraphMaterialVersionRestoreCommand(ContentRef materialRef, long versionNo, long materialLockVersion) {}

package com.thundax.kuzhambu.classics.application.wangqi.command;

import com.thundax.kuzhambu.classics.domain.common.model.valueobject.StorageObjectId;
import com.thundax.kuzhambu.classics.domain.wangqi.model.valueobject.WangqiDocumentId;

public record WangqiDocumentStorageObjectCommand(WangqiDocumentId id, StorageObjectId storageObjectId) {}

package com.thundax.kuzhambu.storage.application.command;

import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StoredObjectId;

public record RemoveStorageObjectCommand(StoredObjectId id) {}

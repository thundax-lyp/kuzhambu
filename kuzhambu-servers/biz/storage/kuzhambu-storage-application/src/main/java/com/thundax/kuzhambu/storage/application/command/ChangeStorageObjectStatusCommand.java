package com.thundax.kuzhambu.storage.application.command;

import com.thundax.kuzhambu.storage.domain.object.model.enums.StoredObjectStatus;
import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StoredObjectId;

public record ChangeStorageObjectStatusCommand(StoredObjectId id, StoredObjectStatus objectStatus) {}

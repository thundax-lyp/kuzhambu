package com.thundax.kuzhambu.storage.application.command;

import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StorageOwnerRef;

public record RemoveStorageReferencesCommand(StorageOwnerRef ownerRef) {}

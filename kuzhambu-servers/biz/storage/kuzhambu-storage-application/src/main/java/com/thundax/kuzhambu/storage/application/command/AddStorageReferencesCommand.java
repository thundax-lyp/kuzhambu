package com.thundax.kuzhambu.storage.application.command;

import com.thundax.kuzhambu.storage.domain.object.model.entity.StoredObjectReference;
import java.util.List;

public record AddStorageReferencesCommand(List<StoredObjectReference> references) {}

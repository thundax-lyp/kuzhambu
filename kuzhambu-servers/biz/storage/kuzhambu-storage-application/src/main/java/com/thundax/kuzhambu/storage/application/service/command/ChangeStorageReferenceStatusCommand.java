package com.thundax.kuzhambu.storage.application.service.command;

import com.thundax.kuzhambu.storage.application.entity.enums.StoredObjectReferenceStatus;
import com.thundax.kuzhambu.storage.domain.object.valueobject.StoredObjectId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChangeStorageReferenceStatusCommand {
    private StoredObjectId id;
    private StoredObjectReferenceStatus referenceStatus;
}

package com.thundax.kuzhambu.biz.storage.service.command;

import com.thundax.kuzhambu.biz.storage.entity.enums.StoredObjectReferenceStatus;
import com.thundax.kuzhambu.biz.storage.entity.valueobject.StoredObjectId;
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

package com.thundax.kuzhambu.biz.storage.service.command;

import com.thundax.kuzhambu.biz.storage.entity.enums.StoredObjectStatus;
import com.thundax.kuzhambu.biz.storage.entity.valueobject.StoredObjectId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChangeStorageObjectStatusCommand {
    private StoredObjectId id;
    private StoredObjectStatus objectStatus;
}

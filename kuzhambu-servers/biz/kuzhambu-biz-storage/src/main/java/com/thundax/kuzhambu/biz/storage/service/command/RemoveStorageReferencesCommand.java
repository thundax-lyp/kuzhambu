package com.thundax.kuzhambu.biz.storage.service.command;

import com.thundax.kuzhambu.biz.storage.entity.enums.StorageOwnerType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class RemoveStorageReferencesCommand {
    private StorageOwnerType ownerType;
    private String ownerId;
}

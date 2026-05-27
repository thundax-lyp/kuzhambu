package com.thundax.kuzhambu.storage.application.service.command;

import com.thundax.kuzhambu.storage.application.entity.enums.StorageOwnerType;
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

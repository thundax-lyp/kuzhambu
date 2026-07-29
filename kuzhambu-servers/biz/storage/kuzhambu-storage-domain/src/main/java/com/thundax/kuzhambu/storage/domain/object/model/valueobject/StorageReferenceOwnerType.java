package com.thundax.kuzhambu.storage.domain.object.model.valueobject;

import com.thundax.kuzhambu.common.core.id.BaseStringId;

public final class StorageReferenceOwnerType extends BaseStringId {

    public StorageReferenceOwnerType(String value) {
        super(normalize(value));
    }

    private static String normalize(String value) {
        return value == null ? null : value.trim();
    }
}

package com.thundax.kuzhambu.storage.application.query;

import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StoredObjectId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GetStorageObjectQuery {
    private StoredObjectId id;
}

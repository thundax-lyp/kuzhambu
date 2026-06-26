package com.thundax.kuzhambu.storage.application.service.result;

import com.thundax.kuzhambu.storage.domain.object.model.valueobject.StoredObjectId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ServerArtifactStoredResult {

    private StoredObjectId storageObjectId;
    private String originalFilename;
    private String contentType;
    private Long sizeBytes;
}

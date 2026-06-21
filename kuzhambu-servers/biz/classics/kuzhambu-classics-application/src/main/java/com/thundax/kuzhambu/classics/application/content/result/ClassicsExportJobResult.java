package com.thundax.kuzhambu.classics.application.content.result;

import com.thundax.kuzhambu.classics.domain.common.model.valueobject.StorageObjectId;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsExportStatus;
import com.thundax.kuzhambu.classics.domain.content.model.valueobject.ClassicsContentExportJobId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ClassicsExportJobResult {

    private ClassicsContentExportJobId jobId;
    private ClassicsExportStatus status;
    private StorageObjectId storageObjectId;
}

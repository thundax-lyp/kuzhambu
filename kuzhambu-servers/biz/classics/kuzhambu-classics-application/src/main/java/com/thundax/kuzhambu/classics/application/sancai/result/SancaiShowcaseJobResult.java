package com.thundax.kuzhambu.classics.application.sancai.result;

import com.thundax.kuzhambu.classics.domain.common.model.valueobject.StorageObjectId;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiShowcaseStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.valueobject.SancaiShowcaseId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SancaiShowcaseJobResult {

    private SancaiShowcaseId showcaseId;
    private SancaiShowcaseStatus status;
    private StorageObjectId storageObjectId;
    private String filename;
    private Long sizeBytes;
    private String sha256;
    private String failureType;
    private String failureMessage;
}

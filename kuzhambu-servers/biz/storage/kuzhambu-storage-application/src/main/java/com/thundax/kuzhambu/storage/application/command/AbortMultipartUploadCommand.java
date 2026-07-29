package com.thundax.kuzhambu.storage.application.command;

import com.thundax.kuzhambu.storage.domain.object.model.valueobject.MultipartUploadId;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AbortMultipartUploadCommand {
    private MultipartUploadId uploadId;
}

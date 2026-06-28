package com.thundax.kuzhambu.storage.application.service.command;

import com.thundax.kuzhambu.storage.domain.object.model.enums.StorageOwnerType;
import java.io.InputStream;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UploadStorageObjectCommand {
    private InputStream inputStream;
    private String originalFilename;
    private String contentType;
    private long size;
    private List<String> allowedSuffixes;
    private StorageOwnerType ownerType;
    private String ownerId;
}

package com.thundax.kuzhambu.operations.application.backup.support;

import java.nio.file.Path;
import lombok.AllArgsConstructor;
import lombok.Getter;

public final class OperationsBackupSupportModels {

    private OperationsBackupSupportModels() {}

    @Getter
    @AllArgsConstructor
    public static class OperationsBackupArtifact {
        private final String baseName;
        private final String fileName;
        private final Path sqlFilePath;
        private final Long fileSizeBytes;
        private final String checksum;
        private final String storageArchiveFilename;
        private final String storageArchiveChecksum;
    }
}

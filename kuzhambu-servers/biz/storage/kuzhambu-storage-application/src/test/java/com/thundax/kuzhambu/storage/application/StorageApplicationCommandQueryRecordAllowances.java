package com.thundax.kuzhambu.storage.application;

import com.thundax.kuzhambu.common.test.architecture.ArchitectureRuleAllowance;
import java.util.List;

final class StorageApplicationCommandQueryRecordAllowances {

    private static final String DESCRIPTION =
            "Legacy application Command/Query is still a Lombok class instead of a record.";
    private static final String REMEDIATION =
            "Convert the contract to a Java record, remove Lombok annotations/imports, update callers, then remove this allowance.";

    private StorageApplicationCommandQueryRecordAllowances() {}

    static List<ArchitectureRuleAllowance> legacyAllowances() {
        return List.of(
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.storage.application.command.InitMultipartUploadCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.storage.application.command.RemoveStorageObjectCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.storage.application.command.RemoveStorageReferencesCommand"),
                legacy("COMMAND_QUERY_RECORD:com.thundax.kuzhambu.storage.application.command.StorageSortCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.storage.application.command.UploadMultipartPartCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.storage.application.command.UploadStorageObjectCommand"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.storage.application.query.GetReadableStorageContentQuery"),
                legacy("COMMAND_QUERY_RECORD:com.thundax.kuzhambu.storage.application.query.GetStorageObjectQuery"),
                legacy("COMMAND_QUERY_RECORD:com.thundax.kuzhambu.storage.application.query.ListStorageMimeTypesQuery"),
                legacy("COMMAND_QUERY_RECORD:com.thundax.kuzhambu.storage.application.query.ListStorageObjectsQuery"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.storage.application.query.ListStorageReferenceOwnerTypesQuery"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.storage.application.query.ListStorageReferencesQuery"),
                legacy(
                        "COMMAND_QUERY_RECORD:com.thundax.kuzhambu.storage.application.query.OpenReadableStorageContentQuery"),
                legacy("COMMAND_QUERY_RECORD:com.thundax.kuzhambu.storage.application.query.StorageQuery"));
    }

    private static ArchitectureRuleAllowance legacy(String key) {
        return ArchitectureRuleAllowance.of(key, DESCRIPTION, REMEDIATION);
    }
}

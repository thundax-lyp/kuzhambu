package com.thundax.kuzhambu.classics.application.content.command;

import com.thundax.kuzhambu.classics.domain.common.model.valueobject.StorageObjectId;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsContentType;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsExportFormat;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsExportKind;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsExportScopeType;
import com.thundax.kuzhambu.classics.domain.content.model.enums.ClassicsExportStatus;
import com.thundax.kuzhambu.classics.domain.sancai.model.enums.SancaiVisibilityRiskStatus;
import java.time.Instant;
import java.util.Set;

public record ContentExportCommand(
        ClassicsExportKind exportKind,
        ClassicsContentType contentType,
        ClassicsExportFormat exportFormat,
        ClassicsExportScopeType scopeType,
        String scopeJson,
        Instant requestedAt,
        Instant expiresAt,
        ClassicsExportStatus status,
        StorageObjectId storageObjectId,
        int itemCount,
        int assetCount,
        SancaiVisibilityRiskStatus visibilityRiskStatus,
        boolean contentChanged,
        Long operatorUserId,
        Set<String> operatorPermissions) {

    public ContentExportCommand(
            ClassicsExportKind exportKind,
            ClassicsContentType contentType,
            ClassicsExportFormat exportFormat,
            ClassicsExportScopeType scopeType,
            String scopeJson,
            Instant requestedAt,
            Instant expiresAt,
            ClassicsExportStatus status,
            StorageObjectId storageObjectId,
            int itemCount,
            int assetCount,
            SancaiVisibilityRiskStatus visibilityRiskStatus,
            boolean contentChanged) {
        this(
                exportKind,
                contentType,
                exportFormat,
                scopeType,
                scopeJson,
                requestedAt,
                expiresAt,
                status,
                storageObjectId,
                itemCount,
                assetCount,
                visibilityRiskStatus,
                contentChanged,
                null,
                null);
    }
}
